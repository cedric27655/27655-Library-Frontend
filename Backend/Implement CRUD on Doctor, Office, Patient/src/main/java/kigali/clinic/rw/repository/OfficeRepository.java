package kigali.clinic.rw.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Office;

@Repository 
public interface OfficeRepository extends JpaRepository<Office, UUID> {
    
    Optional<Office> findByOfficeNumber(int officeNumber);

    // A3 (JPQL): Offices that no doctor occupies, sorted by office number
    @Query("SELECT o FROM Office o WHERE NOT EXISTS (SELECT d FROM Doctor d WHERE d.office = o) ORDER BY o.officeNumber ASC")
    List<Office> findFreeOffices();

    // C3 (JPQL with 3-way JOIN, ORDER BY COUNT DESC): Office whose doctor has the most appointments
    @Query("SELECT a.doctor.office.name, a.doctor.office.officeNumber, COUNT(a) " +
           "FROM Appointment a " +
           "WHERE a.doctor.office IS NOT NULL " +
           "GROUP BY a.doctor.office.name, a.doctor.office.officeNumber " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findBusiestOffice();
}
