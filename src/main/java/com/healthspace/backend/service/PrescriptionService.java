package com.healthspace.backend.service;

import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    // Logic for a doctor to create a new prescription
    public Prescription createPrescription(Prescription prescription) {
        prescription.setDateIssued(LocalDate.now());
        return prescriptionRepository.save(prescription);
    }

    // Logic to get all prescriptions for a patient
    public List<Prescription> getPrescriptionsForPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    // Logic to get all prescriptions written by a doctor
    public List<Prescription> getPrescriptionsForDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }
}