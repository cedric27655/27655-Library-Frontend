package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Optional<Patient> findById(UUID id) {
        return patientRepository.findById(id);
    }

    public Patient create(String firstName, String lastName, LocalDate dateOfBirth) {
        return create(firstName, lastName, dateOfBirth, null);
    }

    public Patient create(String firstName, String lastName, LocalDate dateOfBirth, String ssNumber) {
        Patient patient = new Patient();
        applyDetails(patient, firstName, lastName, dateOfBirth, ssNumber);
        return patientRepository.save(patient);
    }

    public Optional<Patient> update(UUID id, String firstName, String lastName, LocalDate dateOfBirth) {
        return update(id, firstName, lastName, dateOfBirth, null);
    }

    public Optional<Patient> update(UUID id, String firstName, String lastName, LocalDate dateOfBirth, String ssNumber) {
        return patientRepository.findById(id).map(patient -> {
            applyDetails(patient, firstName, lastName, dateOfBirth, ssNumber);
            return patientRepository.save(patient);
        });
    }

    public boolean delete(UUID id) {
        if (!patientRepository.existsById(id)) {
            return false;
        }
        patientRepository.deleteById(id);
        return true;
    }

    // A1: Find patients by last name
    public List<Patient> findByLastName(String lastName) {
        return patientRepository.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    // B4: Patients of a doctor (returns Optional.empty() if doctor id does not exist)
    public Optional<List<Patient>> findPatientsByDoctor(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            return Optional.empty();
        }
        return Optional.of(patientRepository.findDistinctPatientsByDoctorId(doctorId));
    }

    // C2: Frequent patients with at least min appointments
    public List<Patient> findFrequentPatients(long min) {
        return patientRepository.findFrequentPatients(min);
    }

    private void applyDetails(Patient patient, String firstName, String lastName, LocalDate dateOfBirth, String ssNumber) {
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateOfBirth(dateOfBirth);
        if (ssNumber != null) {
            patient.setSsNumber(ssNumber);
        }
    }
}