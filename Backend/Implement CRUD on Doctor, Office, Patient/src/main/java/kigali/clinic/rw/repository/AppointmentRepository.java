package kigali.clinic.rw.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Patient;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // A2 (DERIVED with enum parameter, OrderBy): Appointments by status, earliest date first
    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    // A3 (DERIVED with Between): Appointments in date range inclusive, ordered by date
    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(LocalDate start, LocalDate end);

    // A4 (DERIVED with existsBy, And, Not): Check if doctor has non-cancelled appointment on date
    boolean existsByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, LocalDate appointmentDate, AppointmentStatus status);

    // B4 (JPQL with DISTINCT, path navigation): Patients who have an appointment with doctor
    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<Patient> findDistinctPatientsByDoctorId(@Param("doctorId") UUID doctorId);

    // C1 (JPQL with GROUP BY, COUNT): Count appointments per status
    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus();

    // C2 (JPQL with GROUP BY, HAVING, ORDER BY COUNT): Patients with at least min appointments, busiest first
    @Query("SELECT a.patient FROM Appointment a GROUP BY a.patient HAVING COUNT(a) >= :min ORDER BY COUNT(a) DESC")
    List<Patient> findFrequentPatients(@Param("min") long min);

    // C3 (JPQL with 3-way JOIN, ORDER BY COUNT DESC): Busiest office
    @Query("SELECT a.doctor.office.name, a.doctor.office.officeNumber, COUNT(a) " +
           "FROM Appointment a " +
           "WHERE a.doctor.office IS NOT NULL " +
           "GROUP BY a.doctor.office.name, a.doctor.office.officeNumber " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findBusiestOffice();

    // C4 (JPQL with UPDATE, @Modifying): Cancel doctor's appointments on date except COMPLETED
    @Modifying
    @Query("UPDATE Appointment a SET a.status = kigali.clinic.rw.domain.AppointmentStatus.CANCELLED " +
           "WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status <> kigali.clinic.rw.domain.AppointmentStatus.COMPLETED")
    int cancelDoctorAppointmentsOnDate(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date);

    // Bonus 2 (JPQL with DELETE, @Modifying): Delete CANCELLED appointments before date
    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.status = kigali.clinic.rw.domain.AppointmentStatus.CANCELLED AND a.appointmentDate < :date")
    int deleteCancelledBefore(@Param("date") LocalDate date);

    // --- Practical Exercise 4 Methods ---
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateAsc(UUID doctorId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.status = kigali.clinic.rw.domain.AppointmentStatus.PENDING AND a.appointmentDate < CURRENT_DATE")
    List<Appointment> findPendingAppointmentsPastDate();

    @Query("SELECT a.patient.firstName || ' ' || a.patient.lastName AS patientName, " +
           "a.doctor.firstName || ' ' || a.doctor.lastName AS doctorName, " +
           "a.appointmentDate AS appointmentDate " +
           "FROM Appointment a WHERE a.status = kigali.clinic.rw.domain.AppointmentStatus.PENDING AND a.appointmentDate < CURRENT_DATE")
    List<Object[]> findPendingAppointmentsPastDateDetails();

    @Query("SELECT d.id, d.firstName, d.lastName, COUNT(a) " +
           "FROM Doctor d LEFT JOIN d.appointments a " +
           "GROUP BY d.id, d.firstName, d.lastName " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countAppointmentsPerDoctorBusiestFirst();

    Page<Appointment> findByDoctorId(UUID doctorId, Pageable pageable);

    // D1 (DERIVED with And, StatusNot): Doctor's appointments on date, without CANCELLED
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, LocalDate appointmentDate, AppointmentStatus status);

    // D2 (DERIVED with GreaterThanEqual, In, OrderBy): Patient's upcoming appointments
    List<Appointment> findByPatient_SsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
            String ssNumber, LocalDate date, List<AppointmentStatus> statuses);

    // D4: countBy
    long countByAppointmentDate(LocalDate date);
    long countByStatus(AppointmentStatus status);
}
