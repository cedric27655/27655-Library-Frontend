package kigali.clinic.rw.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    // A1 (DERIVED): Find patients by last name, case-insensitive, sorted by first name A to Z
    List<Patient> findByLastNameIgnoreCaseOrderByFirstNameAsc(String lastName);

    // B4 (JPQL with DISTINCT and path navigation): Patients who have an appointment with a doctor
    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<Patient> findDistinctPatientsByDoctorId(@Param("doctorId") UUID doctorId);

    // Derived methods for ssNumber
    Optional<Patient> findBySsNumber(String ssNumber);
    boolean existsBySsNumber(String ssNumber);

    // C2 (JPQL with GROUP BY, HAVING, ORDER BY COUNT): Frequent patients with at least min appointments
    @Query("SELECT a.patient FROM Appointment a GROUP BY a.patient HAVING COUNT(a) >= :min ORDER BY COUNT(a) DESC")
    List<Patient> findFrequentPatients(@Param("min") long min);
}