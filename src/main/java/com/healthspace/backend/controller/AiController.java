// java
package com.healthspace.backend.controller;

import com.healthspace.backend.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/consult")
    public Map<String, String> consultAi(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.get("query");

        // Extract patientId safely (might be null for guests)
        Long patientId = null;
        if (payload.get("patientId") != null) {
            try {
                patientId = Long.valueOf(payload.get("patientId").toString());
            } catch (NumberFormatException e) {
                // Ignore if ID is malformed
            }
        }

        // Get Intelligence with Context (returns Map<String, Object>)
        Map<String, Object> aiResult = aiService.getConsultation(query, patientId);

        // Convert Map<String, Object> to Map<String, String> for cleaner response
        Map<String, String> response = new HashMap<>();

        // Mandatory: Reply text
        response.put("text", (String) aiResult.get("reply"));

        // Optional: Doctor ID
        Object doctorId = aiResult.get("recommended_doctor_id");
        if (doctorId != null) {
            response.put("recommended_doctor_id", doctorId.toString());
            // NEW: Include descriptive fields for the frontend
            response.put("recommended_doctor_name", (String) aiResult.getOrDefault("recommended_doctor_name", "Doctor"));
            response.put("recommended_doctor_specialty", (String) aiResult.getOrDefault("recommended_doctor_specialty", "N/A"));
        }

        return response;
    }
}
