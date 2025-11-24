package com.healthspace.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthspace.backend.entity.Appointment;
import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.PatientProfile;
import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.repository.AppointmentRepository;
import com.healthspace.backend.repository.DoctorProfileRepository;
import com.healthspace.backend.repository.PatientProfileRepository;
import com.healthspace.backend.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiService {

    @Value("${app.gemini.api-key}")
    private String geminiKey;

    @Value("${app.gemini.url}")
    private String geminiUrl;

    @Autowired
    private DoctorProfileRepository doctorRepository;
    @Autowired
    private PatientProfileRepository patientRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PrescriptionRepository prescriptionRepository;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getConsultation(String userQuery, Long patientId) {
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("reply", "I apologize, but I am currently unable to access the medical database.");
        finalResponse.put("recommended_doctor_id", null);

        try {
            // 1. BUILD RICH CONTEXT
            List<DoctorProfile> doctors = doctorRepository.findByAffiliationStatus("VERIFIED");

            String doctorList = doctors.isEmpty() ? "No verified doctors available." : doctors.stream()
                    .map(d -> {
                        String hospitalName = d.getHospital() != null ? d.getHospital().getName() : "Unaffiliated";
                        String city = d.getHospital() != null ? d.getHospital().getCity() : "Unknown";
                        String address = d.getHospital() != null ? d.getHospital().getAddress() : "";

                        // Handle Contact Info safely
                        String contact = "N/A";
                        if (d.getHospital() != null) {
                            if (d.getHospital().getWebsite() != null && !d.getHospital().getWebsite().isEmpty()) {
                                contact = d.getHospital().getWebsite();
                            } else if (d.getHospital().getContactPhone() != null) {
                                contact = d.getHospital().getContactPhone();
                            }
                        }

                        // SAFE CONVERSION for Fee and Experience to avoid IllegalFormatConversionException
                        double fee = d.getConsultationFee() != null ? d.getConsultationFee() : 0.0;
                        int exp = d.getExperienceYears() != null ? d.getExperienceYears() : 0;

                        // We use %s for strings, %.0f for floating point numbers, %d for integers
                        return String.format("| %d | Dr. %s | %s | %s | %s | %s | %s | %s | ₹%.0f | %d yrs |",
                                d.getUser().getId(),
                                d.getUser().getName(),
                                d.getDegree() != null ? d.getDegree() : "MBBS",
                                d.getSpecialty(),
                                hospitalName,
                                address,
                                city,
                                contact,
                                fee, // %.0f expects double/float
                                exp  // %d expects int/long
                        );
                    })
                    .collect(Collectors.joining("\n"));

            String patientContext = "Guest User (No Record)";
            String historyContext = "No medical history available.";
            String scheduleContext = "No upcoming appointments.";

            if (patientId != null) {
                Optional<PatientProfile> pOpt = patientRepository.findByUserId(patientId);
                if (pOpt.isPresent()) {
                    PatientProfile p = pOpt.get();
                    // Safety check for height/weight formatting
                    double height = p.getHeight() != null ? p.getHeight() : 0.0;
                    double weight = p.getWeight() != null ? p.getWeight() : 0.0;

                    patientContext = String.format("Name: %s, Age: %s, Gender: %s, Blood: %s, Height: %.1f, Weight: %.1f",
                            p.getUser().getName(), p.getDob(), p.getGender(), p.getBloodGroup(), height, weight);
                }

                List<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
                if (!prescriptions.isEmpty()) {
                    historyContext = prescriptions.stream()
                            .limit(5)
                            .map(rx -> String.format("- %s: %s (Notes: %s)",
                                    rx.getCreatedAt().toLocalDate(), rx.getDiagnosis(),
                                    rx.getNotes() != null ? rx.getNotes() : "None"))
                            .collect(Collectors.joining("\n"));
                }

                List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
                if (!appointments.isEmpty()) {
                    scheduleContext = appointments.stream()
                            .filter(a -> "BOOKED".equals(a.getStatus()))
                            .map(a -> String.format("- %s with Doctor ID %d (%s)",
                                    a.getAppointmentTime(), a.getDoctorId(),
                                    a.getSymptoms() != null ? a.getSymptoms() : "Check-up"))
                            .collect(Collectors.joining("\n"));
                }
            }

            // 2. SYSTEM PROMPT
            String systemPrompt = """
                Act as 'HealthSpace AI', a medical assistant connected to a live database.
                
                === DATA CONTEXT ===
                USER: %s
                HISTORY: %s
                SCHEDULE: %s
                
                === DOCTOR & HOSPITAL DATABASE (Internal Records) ===
                | ID | Name | Degree | Specialty | Hospital | Address | City | Contact | Fee | Exp |
                |---|---|---|---|---|---|---|---|---|---|
                %s

                === RULES ===
                1. **CONTENT RICH:** Use degrees, addresses, and specialties to provide detailed answers.
                
                2. **STRICT LOCATION CHECK:** Verify 'City' and 'Address'. Do not hallucinate locations not in the list.
                
                3. **VISUAL FORMATTING (IMPORTANT):** - When listing doctors, use a Markdown Table.
                   - **HIDE THE ID COLUMN:** Do NOT show the 'ID' column in the table visible to the user. Only show Name, Specialty, Hospital, Fee, etc.
                   - **Bold** doctor names.
                   - *Italicize* specialties.
                
                4. **BOOKING:** If the user wants to book, use the internal ID to fill the 'doctor_id' field in the JSON below.

                5. **OUTPUT JSON:**
                {
                    "reply": "Markdown text (Table WITHOUT ID column)...",
                    "doctor_id": 123 (or null)
                }
                """.formatted(patientContext, historyContext, scheduleContext, doctorList);

            // 3. CALL GEMINI
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", systemPrompt + "\n\nUSER QUERY: " + userQuery)))),
                    "model", "gemini-2.5-flash"
            );

            String response = restClient.post()
                    .uri(geminiUrl)
                    .header("X-goog-api-key", geminiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String rawText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            String cleanJson = rawText.trim().replace("```json", "").replace("```", "").trim();

            Map<String, Object> aiResult = objectMapper.readValue(cleanJson, Map.class);

            // Post-process
            Object doctorIdObj = aiResult.get("doctor_id");
            if (doctorIdObj != null) {
                try {
                    Long docIdLong = Long.valueOf(doctorIdObj.toString());
                    DoctorProfile docProfile = doctorRepository.findByUserId(docIdLong).orElse(null);
                    if (docProfile != null) {
                        finalResponse.put("recommended_doctor_id", docIdLong);
                        finalResponse.put("recommended_doctor_name", docProfile.getUser().getName());
                        finalResponse.put("recommended_doctor_specialty", docProfile.getSpecialty());
                    }
                } catch (Exception e) {}
            }

            finalResponse.put("reply", (String) aiResult.get("reply"));
            return finalResponse;

        } catch (Exception e) {
            e.printStackTrace();
            // Return a friendly error message if backend logic fails, but log the stack trace for debugging
            return finalResponse;
        }
    }
}