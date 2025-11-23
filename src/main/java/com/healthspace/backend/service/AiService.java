package com.healthspace.backend.service;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.PatientProfile;
import com.healthspace.backend.repository.DoctorProfileRepository;
import com.healthspace.backend.repository.PatientProfileRepository;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final ChatClient chatClient;

    @Autowired
    private DoctorProfileRepository doctorRepository;

    @Autowired
    private PatientProfileRepository patientRepository;

    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    // Now accepts history + userId for context
    public String chatWithAi(String userMessage, Long patientId, List<Map<String, String>> history) {

        // 1. Fetch Data for Context
        List<DoctorProfile> doctors = doctorRepository.findByAffiliationStatus("VERIFIED");

        String doctorListString = doctors.stream()
                .map(d -> String.format("[ID:%d] Dr. %s (%s) at %s in %s. Fee: %s",
                        d.getUser().getId(), d.getUser().getName(), d.getSpecialty(),
                        d.getHospital().getName(), d.getHospital().getCity(), d.getConsultationFee()))
                .collect(Collectors.joining("\n"));

        String patientContext = "Guest User";
        if(patientId != null) {
            PatientProfile p = patientRepository.findByUserId(patientId).orElse(null);
            if(p != null) {
                patientContext = String.format("Name: %s, Age: %s, Gender: %s, City: %s",
                        p.getUser().getName(), p.getDob(), p.getGender(), p.getAddress());
            }
        }

        // 2. The "Guardrail" System Prompt
        String systemText = """
            You are 'HealthSpace Assistant', a warm and empathetic medical guide.
            
            CURRENT PATIENT CONTEXT:
            %s
            
            AVAILABLE DOCTOR NETWORK:
            %s
            
            YOUR RULES:
            1. **MEDICAL SAFETY:** NEVER prescribe medicines (like 'take Paracetamol'). NEVER suggest dosages.
            2. **SUGGESTIONS:** You CAN suggest: home remedies (rest, hydration), lifestyle changes, or which specialist to see.
            3. **booking:** If the user seems sick, ALWAYS recommend a doctor from the list above based on their specialty and city.
            4. **FORMAT:** If recommending a doctor, format it clearly. If the user says "Book Dr. X", return a special JSON tag: <BOOK_DOCTOR_ID: 123>.
            5. **TONE:** Be brief, professional, but caring.
            """.formatted(patientContext, doctorListString);

        // 3. Build Message History (Context Window)
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemText));

        // Replay history so AI "remembers"
        if (history != null) {
            for (Map<String, String> msg : history) {
                if ("user".equals(msg.get("role"))) {
                    messages.add(new UserMessage(msg.get("content")));
                } else {
                    messages.add(new AssistantMessage(msg.get("content")));
                }
            }
        }

        // Add current message
        messages.add(new UserMessage(userMessage));

        // 4. Call AI
        return chatClient.call(new Prompt(messages)).getResult().getOutput().getContent();
    }
}