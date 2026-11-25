package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class SpecializationService {
    private final SpecializationRepository specializationRepository;
    private final DoctorRepository doctorRepository;

    public SpecializationService(SpecializationRepository specializationRepository, DoctorRepository doctorRepository) {
        this.specializationRepository = specializationRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Specialization> findAll() {
        return specializationRepository.findAll();
    }

    public Optional<Specialization> findById(UUID id) {
        return specializationRepository.findById(id);
    }

    public Specialization create(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialization name is required");
        }
        if (specializationRepository.existsByNameIgnoreCase(name.trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Specialization with this name already exists");
        }
        Specialization specialization = new Specialization();
        specialization.setName(name.trim());
        return specializationRepository.save(specialization);
    }

    public Optional<Specialization> update(UUID id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialization name is required");
        }
        Optional<Specialization> found = specializationRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        Optional<Specialization> existing = specializationRepository.findByNameIgnoreCase(name.trim());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Specialization with this name already exists");
        }

        Specialization specialization = found.get();
        specialization.setName(name.trim());
        return Optional.of(specializationRepository.save(specialization));
    }

    public boolean delete(UUID id) {
        Optional<Specialization> found = specializationRepository.findById(id);
        if (found.isEmpty()) {
            return false;
        }
        Specialization specialization = found.get();
        // Remove associations from doctors
        for (Doctor doctor : specialization.getDoctors()) {
            doctor.getSpecializations().remove(specialization);
            doctorRepository.save(doctor);
        }
        specializationRepository.delete(specialization);
        return true;
    }

    public List<Doctor> getDoctors(UUID specializationId) {
        Specialization spec = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specialization not found"));
        return spec.getDoctors();
    }

    // B3: Specializations nobody offers
    public List<Specialization> findUnusedSpecializations() {
        return specializationRepository.findUnusedSpecializations();
    }
}
