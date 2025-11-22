package com.healthspace.backend.service;

import com.healthspace.backend.dto.PrescriptionItemRequest;
import com.healthspace.backend.dto.PrescriptionRequest;
import com.healthspace.backend.entity.Appointment;
import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.entity.PrescriptionItem;
import com.healthspace.backend.repository.AppointmentRepository;
import com.healthspace.backend.repository.PrescriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Transactional // Important: Either everything saves, or nothing saves.
    public Prescription createPrescription(PrescriptionRequest request, Long doctorId) {

        // 1. Validate Appointment Context
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new SecurityException("You are not the authorized doctor for this appointment.");
        }

        // 2. Create Prescription Header
        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setPatientId(appointment.getPatientId());
        prescription.setDoctorId(doctorId);
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setNotes(request.getNotes());

        // 3. Map Items (The List Logic)
        List<PrescriptionItem> items = new ArrayList<>();
        for (PrescriptionItemRequest itemReq : request.getMedicines()) {
            PrescriptionItem item = new PrescriptionItem();
            item.setMedicineName(itemReq.getMedicineName());
            item.setDosage(itemReq.getDosage());
            item.setFrequency(itemReq.getFrequency());
            item.setDuration(itemReq.getDuration());
            item.setPrescription(prescription); // Link child to parent
            items.add(item);
        }

        prescription.setMedicines(items); // Link parent to children

        // 4. Update Appointment Status
        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);

        // 5. Save
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getPrescriptionsForPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }
}