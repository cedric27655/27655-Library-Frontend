package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final SpecializationRepository specializationRepository;

    public DoctorService(DoctorRepository doctorRepository, OfficeRepository officeRepository, SpecializationRepository specializationRepository) {
        this.doctorRepository = doctorRepository;
        this.officeRepository = officeRepository;
        this.specializationRepository = specializationRepository;
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> findById(UUID id) {
        return doctorRepository.findById(id);
    }

    public Doctor create(String firstName, String lastName, LocalDate dateOfBirth, UUID officeId) {
        return create(firstName, lastName, dateOfBirth, officeId, null);
    }

    public Doctor create(String firstName, String lastName, LocalDate dateOfBirth, UUID officeId, List<UUID> specializationIds) {
        Doctor doctor = new Doctor();
        applyDetails(doctor, firstName, lastName, dateOfBirth, officeId, specializationIds);
        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> update(UUID id, String firstName, String lastName, LocalDate dateOfBirth, UUID officeId) {
        return update(id, firstName, lastName, dateOfBirth, officeId, null);
    }

    public Optional<Doctor> update(UUID id, String firstName, String lastName, LocalDate dateOfBirth, UUID officeId, List<UUID> specializationIds) {
        return doctorRepository.findById(id).map(doctor -> {
            applyDetails(doctor, firstName, lastName, dateOfBirth, officeId, specializationIds);
            return doctorRepository.save(doctor);
        });
    }

    public boolean delete(UUID id) {
        Optional<Doctor> found = doctorRepository.findById(id);
        if (found.isEmpty()) {
            return false;
        }
        Doctor doctor = found.get();
        doctor.setOffice(null);
        doctor.getSpecializations().clear();
        doctorRepository.save(doctor);
        doctorRepository.delete(doctor);
        return true;
    }

    // A1: Add one specialization to doctor
    public String addSpecialization(UUID doctorId, UUID specializationId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return "The doctor with that id does not exist";
        }
        Optional<Specialization> specOpt = specializationRepository.findById(specializationId);
        if (specOpt.isEmpty()) {
            return "That specialization does not exist";
        }

        Doctor doctor = doctorOpt.get();
        Specialization specialization = specOpt.get();

        if (doctor.getSpecializations().contains(specialization)) {
            return "The doctor already has that specialization";
        }

        doctor.getSpecializations().add(specialization);
        doctorRepository.save(doctor);
        return "Specialization added to the doctor";
    }

    // A2: Remove one specialization from doctor
    public String removeSpecialization(UUID doctorId, UUID specializationId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return "The doctor with that id does not exist";
        }
        Optional<Specialization> specOpt = specializationRepository.findById(specializationId);
        if (specOpt.isEmpty()) {
            return "That specialization does not exist";
        }

        Doctor doctor = doctorOpt.get();
        Specialization specialization = specOpt.get();

        if (!doctor.getSpecializations().contains(specialization)) {
            return "The doctor does not have that specialization";
        }

        doctor.getSpecializations().remove(specialization);
        doctorRepository.save(doctor);
        return "Specialization removed from the doctor";
    }

    // A4: Move doctor to another office
    public String moveDoctorOffice(UUID doctorId, int officeNumber) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return "The doctor with that id does not exist";
        }
        Optional<Office> officeOpt = officeRepository.findByOfficeNumber(officeNumber);
        if (officeOpt.isEmpty()) {
            return "The office with that office number does not exist";
        }

        Office targetOffice = officeOpt.get();
        Doctor doctor = doctorOpt.get();

        // Check if office is already occupied by another doctor using existsBy
        boolean occupiedByOther = doctorRepository.existsByOffice_IdAndIdNot(targetOffice.getId(), doctorId);
        if (occupiedByOther) {
            return "That office is taken by other doctor";
        }

        doctor.setOffice(targetOffice);
        doctorRepository.save(doctor);
        return "Office changed successfully";
    }

    public List<Specialization> getSpecializations(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        return doctor.getSpecializations();
    }

    // B1: Doctors who hold that specialization (case-insensitive)
    public List<Doctor> findBySpecializationName(String name) {
        return doctorRepository.findBySpecializationName(name);
    }

    // B2: Doctors who have no office assigned, sorted by last name
    public List<Doctor> findDoctorsWithoutOffice() {
        return doctorRepository.findDoctorsWithoutOffice();
    }

    // D3: Search doctors by name
    public List<Doctor> searchByName(String name) {
        return doctorRepository.searchByName(name);
    }

    // Bonus 2: Doctor workload
    public List<Object[]> getDoctorWorkload(LocalDate start, LocalDate end) {
        return doctorRepository.findDoctorWorkload(start, end);
    }

    private void applyDetails(Doctor doctor, String firstName, String lastName, LocalDate dateOfBirth, UUID officeId, List<UUID> specializationIds) {
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setDateOfBirth(dateOfBirth);
        Office office = null;
        if (officeId != null) {
            office = officeRepository.findById(officeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Office not found"));
            boolean occupied = doctor.getId() == null
                    ? doctorRepository.existsByOffice_Id(officeId)
                    : doctorRepository.existsByOffice_IdAndIdNot(officeId, doctor.getId());
            if (occupied) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Office is already assigned to another doctor");
            }
        }
        doctor.setOffice(office);

        if (specializationIds != null) {
            List<Specialization> list = new ArrayList<>();
            for (UUID specId : specializationIds) {
                Specialization spec = specializationRepository.findById(specId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specialization not found: " + specId));
                list.add(spec);
            }
            doctor.setSpecializations(list);
        }
    }
}