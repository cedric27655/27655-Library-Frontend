package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class AppointmentService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public AppointmentService(DoctorRepository doctorRepository, PatientRepository patientRepository,
                              AppointmentRepository appointmentRepository, PrescriptionRepository prescriptionRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    // B1: Safe booking (reject past dates, force SCHEDULED)
    public String saveAppointment(Appointment appointment, UUID patientId, UUID doctorId) {
        if (patientId == null || doctorId == null) {
            return "That patient or doctor is not available";
        }

        if (appointment.getAppointmentDate() != null && appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }

        Optional<Doctor> getDoctor = doctorRepository.findById(doctorId);
        Optional<Patient> getPatient = patientRepository.findById(patientId);

        if (getPatient.isEmpty() || getDoctor.isEmpty()) {
            return "That patient or doctor is not available";
        }

        // Check double booking
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatusNot(
                doctorId, appointment.getAppointmentDate(), AppointmentStatus.CANCELLED)) {
            return "Doctor is already booked on that date";
        }

        // New appointment always starts as SCHEDULED
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setDoctor(getDoctor.get());
        appointment.setPatient(getPatient.get());
        appointmentRepository.save(appointment);

        return "Appointment is saved successfully";
    }

    // B2: Status flow
    public String updateStatus(UUID id, AppointmentStatus newStatus) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            return "The appointment with that id does not exist";
        }

        Appointment appointment = opt.get();
        AppointmentStatus current = appointment.getStatus();

        boolean allowed = false;
        if (current == AppointmentStatus.SCHEDULED && (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED)) {
            allowed = true;
        } else if (current == AppointmentStatus.CONFIRMED && (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED)) {
            allowed = true;
        }

        if (!allowed) {
            return "Cannot change status from " + current + " to " + newStatus;
        }

        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);
        return "Appointment status updated successfully";
    }

    // B3: Reschedule an appointment
    public String reschedule(UUID id, LocalDate newDate) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            return "The appointment with that id does not exist";
        }

        Appointment appointment = opt.get();
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return "A COMPLETED appointment cannot be rescheduled";
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return "A CANCELLED appointment cannot be rescheduled";
        }

        if (newDate.isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }

        // Check if doctor has a non-cancelled appointment on newDate
        if (appointment.getDoctor() != null && appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatusNot(
                appointment.getDoctor().getId(), newDate, AppointmentStatus.CANCELLED)) {
            return "Doctor is already booked on that date";
        }

        appointment.setAppointmentDate(newDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);
        return "Appointment rescheduled successfully";
    }

    // C4 (protect): Delete appointment if it has no prescriptions
    public String deleteAppointment(UUID id) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            return "The appointment with that id does not exist";
        }

        if (prescriptionRepository.existsByAppointment_Id(id)) {
            return "That appointment has prescriptions, delete them first";
        }

        appointmentRepository.deleteById(id);
        return "Appointment deleted successfully";
    }

    // D1 (DERIVED): Doctor's agenda for a day without CANCELLED
    public Optional<List<Appointment>> getDoctorAgenda(UUID doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            return Optional.empty();
        }
        return Optional.of(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusNot(
                doctorId, date, AppointmentStatus.CANCELLED));
    }

    // D2 (DERIVED): Patient's upcoming appointments
    public Optional<List<Appointment>> getPatientUpcoming(String ssNumber) {
        if (!patientRepository.existsBySsNumber(ssNumber)) {
            return Optional.empty();
        }
        return Optional.of(appointmentRepository.findByPatient_SsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
                ssNumber, LocalDate.now(), List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED)));
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> findById(UUID id) {
        return appointmentRepository.findById(id);
    }

    // A2 (DERIVED): Appointments by status
    public List<Appointment> findByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByAppointmentDateAsc(status);
    }

    // A3 (DERIVED): Appointments in date range inclusive, ordered by date
    public List<Appointment> findBetween(LocalDate start, LocalDate end) {
        return appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start, end);
    }

    // C1 (JPQL): Count appointments per status
    public List<Object[]> countAppointmentsByStatus() {
        return appointmentRepository.countAppointmentsByStatus();
    }

    // C4 (JPQL): Doctor is absent - cancel appointments on date except COMPLETED
    @Transactional
    public String cancelDoctorAppointmentsOnDate(UUID doctorId, LocalDate date) {
        int count = appointmentRepository.cancelDoctorAppointmentsOnDate(doctorId, date);
        return count + " appointments cancelled";
    }

    // Bonus 1: Appointments page by page
    public Page<Appointment> findAllPaged(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    // Bonus 2 (JPQL): Delete CANCELLED appointments before date
    @Transactional
    public String deleteCancelledBefore(LocalDate date) {
        int count = appointmentRepository.deleteCancelledBefore(date);
        return count + " appointments deleted";
    }
}
