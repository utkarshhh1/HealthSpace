package com.healthspace.backend.service;

import com.healthspace.backend.entity.Appointment; // <-- NEW IMPORT
import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.repository.AppointmentRepository; // <-- NEW IMPORT
import com.healthspace.backend.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository; // <-- INJECT THE APPOINTMENT REPO

    // --- THIS METHOD IS NOW SMARTER ---
    public Prescription createPrescription(Prescription prescription) {

        // --- NEW VALIDATION LOGIC ---
        // 1. Get the IDs from the incoming prescription request
        Long appId = prescription.getAppointmentId();
        Long patientId = prescription.getPatientId();
        Long doctorId = prescription.getDoctorId();

        // 2. Find the appointment in the database
        Appointment appointment = appointmentRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appId));

        // 3. Verify that the appointment's patient and doctor match the prescription
        if (!appointment.getPatientId().equals(patientId) || !appointment.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Error: Prescription patient/doctor do not match the appointment record.");
        }

        // 4. (Optional) You could also check if the appointment status is "COMPLETED"
        // if (!"COMPLETED".equals(appointment.getStatus())) {
        //     throw new RuntimeException("Cannot create prescription for an appointment that is not completed.");
        // }
        // --- END OF VALIDATION ---

        // If all checks pass, save the prescription
        prescription.setDateIssued(LocalDate.now());
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getPrescriptionsForPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public List<Prescription> getPrescriptionsForDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }
}