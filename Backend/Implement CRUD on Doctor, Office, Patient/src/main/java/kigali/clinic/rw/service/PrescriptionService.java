package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository, AppointmentRepository appointmentRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // C1: Write a prescription
    public String writePrescription(Prescription prescription, UUID appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            return "The appointment with that id does not exist";
        }

        Appointment appointment = appointmentOpt.get();
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            return "Prescriptions are only for COMPLETED appointments";
        }

        prescription.setIssuedDate(LocalDate.now());
        prescription.setAppointment(appointment);
        prescriptionRepository.save(prescription);
        return "Prescription saved successfully";
    }

    // C2 (DERIVED): Prescriptions of an appointment
    public Optional<List<Prescription>> getPrescriptionsByAppointment(UUID appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            return Optional.empty();
        }
        return Optional.of(prescriptionRepository.findByAppointment_Id(appointmentId));
    }

    // C3 (JPQL): Prescription history of a patient
    public List<Prescription> getPrescriptionHistory(String ssNumber) {
        return prescriptionRepository.findByPatientSsNumberOrderByIssuedDateDesc(ssNumber);
    }

    // C4: Update prescription (changes only medicineName, dosage, and durationDays)
    public String updatePrescription(UUID id, Prescription updateData) {
        Optional<Prescription> opt = prescriptionRepository.findById(id);
        if (opt.isEmpty()) {
            return "The prescription with that id does not exist";
        }

        Prescription existing = opt.get();
        existing.setMedicineName(updateData.getMedicineName());
        existing.setDosage(updateData.getDosage());
        existing.setDurationDays(updateData.getDurationDays());
        prescriptionRepository.save(existing);
        return "Prescription updated successfully";
    }

    // C4: Delete prescription
    public String deletePrescription(UUID id) {
        Optional<Prescription> opt = prescriptionRepository.findById(id);
        if (opt.isEmpty()) {
            return "The prescription with that id does not exist";
        }

        prescriptionRepository.deleteById(id);
        return "Prescription deleted successfully";
    }

    // Bonus 1: Most prescribed medicines
    public List<Object[]> getTopMedicines() {
        return prescriptionRepository.findTopMedicines();
    }
}
