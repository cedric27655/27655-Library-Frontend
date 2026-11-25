package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;

@Service 
public class OfficeService {
  private final OfficeRepository officeRepo;
  private final DoctorRepository doctorRepo;

  public OfficeService(OfficeRepository officeRepo, DoctorRepository doctorRepo) {
    this.officeRepo = officeRepo;
    this.doctorRepo = doctorRepo;
  }

  public List<Office> findAll() {
    return officeRepo.findAll();
  }

  public Optional<Office> findById(UUID id) {
    return officeRepo.findById(id);
  }

  public Office create(String name, int officeNumber) {
    if (officeRepo.findByOfficeNumber(officeNumber).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Office number already exists");
    }

    Office office = new Office();
    office.setName(name);
    office.setOfficeNumber(officeNumber);
    return officeRepo.save(office);
  }

  public Optional<Office> update(UUID id, String name, int officeNumber) {
    Optional<Office> foundOffice = officeRepo.findById(id);
    if (foundOffice.isEmpty()) {
      return Optional.empty();
    }

    Optional<Office> duplicate = officeRepo.findByOfficeNumber(officeNumber);
    if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Office number already exists");
    }

    Office office = foundOffice.get();
    office.setName(name);
    office.setOfficeNumber(officeNumber);
    return Optional.of(officeRepo.save(office));
  }

  @Transactional
  public boolean delete(UUID id) {
    if (!officeRepo.existsById(id)) {
      return false;
    }
    // If a doctor is assigned to this office, unassign them first so the foreign key doesn't block deletion
    doctorRepo.findByOffice_Id(id).ifPresent(doctor -> {
      doctor.setOffice(null);
      doctorRepo.save(doctor);
    });
    officeRepo.deleteById(id);
    return true;
  }

  // A3: Free offices
  public List<Office> findFreeOffices() {
    return officeRepo.findFreeOffices();
  }

  // C3: Busiest office
  public Object findBusiestOffice() {
    List<Object[]> results = officeRepo.findBusiestOffice();
    if (results.isEmpty()) {
      return "No appointments yet";
    }
    return results.get(0);
  }
}
