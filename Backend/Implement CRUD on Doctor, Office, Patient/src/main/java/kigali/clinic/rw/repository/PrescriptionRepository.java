package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    // C2 (DERIVED): Prescriptions of an appointment
    List<Prescription> findByAppointment_Id(UUID appointmentId);

    // C3 (JPQL): Prescription history of a patient using single-valued path navigation
    @Query("SELECT p FROM Prescription p WHERE p.appointment.patient.ssNumber = :ssNumber ORDER BY p.issuedDate DESC")
    List<Prescription> findByPatientSsNumberOrderByIssuedDateDesc(@Param("ssNumber") String ssNumber);

    // C4 (DERIVED): Check if any prescriptions exist for an appointment
    boolean existsByAppointment_Id(UUID appointmentId);

    // Bonus 1 (JPQL): Most prescribed medicines
    @Query("SELECT p.medicineName, COUNT(p) FROM Prescription p GROUP BY p.medicineName ORDER BY COUNT(p) DESC")
    List<Object[]> findTopMedicines();
}
