package com.healthspace.backend.service;

import com.healthspace.backend.dto.PrescriptionItemRequest;
import com.healthspace.backend.dto.PrescriptionRequest;
import com.healthspace.backend.entity.Appointment;
import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.entity.PrescriptionItem;
import com.healthspace.backend.repository.AppointmentRepository;
import com.healthspace.backend.repository.DoctorProfileRepository;
import com.healthspace.backend.repository.PrescriptionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    /**
     * Create a prescription. Validates appointment & doctor-subject; updates appointment status to COMPLETED.
     */
    @Transactional
    public Prescription createPrescription(PrescriptionRequest request, Long doctorId) {
        if (request == null || request.getAppointmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment ID is required");
        }

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        // Ensure the doctor issuing the prescription is assigned to that appointment
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the authorized doctor for this appointment.");
        }

        // Optional: ensure doctor is verified before issuing (if profile exists)
        DoctorProfile docProfile = doctorProfileRepository.findByUserId(doctorId).orElse(null);
        if (docProfile != null && !"VERIFIED".equalsIgnoreCase(docProfile.getAffiliationStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor account not verified to issue prescriptions");
        }

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setPatientId(appointment.getPatientId());
        prescription.setDoctorId(doctorId);
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setNotes(request.getNotes());

        List<PrescriptionItem> items = new ArrayList<>();
        if (request.getMedicines() != null) {
            for (PrescriptionItemRequest itemReq : request.getMedicines()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(itemReq.getMedicineName());
                item.setDosage(itemReq.getDosage());
                item.setFrequency(itemReq.getFrequency());
                item.setDuration(itemReq.getDuration());
                item.setPrescription(prescription);
                items.add(item);
            }
        }
        prescription.setMedicines(items);

        // Mark appointment as completed (idempotent)
        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);

        Prescription saved = prescriptionRepository.save(prescription);
        logger.info("Created prescription id={} appointmentId={} doctorId={}", saved.getId(), request.getAppointmentId(), doctorId);
        return saved;
    }

    public List<Prescription> getPrescriptionsForPatient(Long patientId) {
        if (patientId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient ID required");
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<Prescription> getPrescriptionsForDoctor(Long doctorId) {
        if (doctorId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor ID required");
        return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }

    public List<Long> getUniquePatientsForDoctor(Long doctorId) {
        List<Prescription> prescriptions = getPrescriptionsForDoctor(doctorId);
        return prescriptions.stream()
                .map(Prescription::getPatientId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Helper used by the controller to check whether a doctorId is actually the
     * assigned doctor for a given appointmentId.
     */
    public boolean isDoctorAssignedToAppointment(Long doctorId, Long appointmentId) {
        if (doctorId == null || appointmentId == null) return false;
        return appointmentRepository.findById(appointmentId)
                .map(a -> doctorId.equals(a.getDoctorId()))
                .orElse(false);
    }
}
