package kigali.clinic.rw.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Doctor;

@Repository 
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    boolean existsByOffice_Id(UUID officeId);
    boolean existsByOffice_IdAndIdNot(UUID officeId, UUID doctorId);
    Optional<Doctor> findByOffice_Id(UUID officeId);

    // B1 (JPQL with JOIN on collection, LOWER): Doctors who hold that specialization (case-insensitive)
    @Query("SELECT d FROM Doctor d JOIN d.specializations s WHERE LOWER(s.name) = LOWER(:name)")
    List<Doctor> findBySpecializationName(@Param("name") String name);

    // B2 (JPQL with IS NULL): Doctors with no office assigned, sorted by last name
    @Query("SELECT d FROM Doctor d WHERE d.office IS NULL ORDER BY d.lastName ASC")
    List<Doctor> findDoctorsWithoutOffice();

    // D3 (JPQL with LIKE, LOWER, OR): Search doctors by first or last name, ordered by last name
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY d.lastName ASC")
    List<Doctor> searchByName(@Param("name") String name);

    // Bonus 2 (JPQL with JOIN, WHERE, BETWEEN, GROUP BY): Doctor workload between dates
    @Query("SELECT a.doctor.firstName, a.doctor.lastName, COUNT(a) " +
           "FROM Appointment a " +
           "WHERE a.status = kigali.clinic.rw.domain.AppointmentStatus.COMPLETED " +
           "AND a.appointmentDate BETWEEN :start AND :end " +
           "GROUP BY a.doctor.firstName, a.doctor.lastName " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findDoctorWorkload(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);
}
