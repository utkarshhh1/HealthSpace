package com.healthspace.backend.dto;

import java.util.List;

public class PrescriptionRequest {
    private Long appointmentId;
    private String diagnosis;
    private String notes;
    private List<PrescriptionItemRequest> medicines; // The list of meds

    // Getters & Setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<PrescriptionItemRequest> getMedicines() { return medicines; }
    public void setMedicines(List<PrescriptionItemRequest> medicines) { this.medicines = medicines; }
}