package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.service.PatientService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/api/patients", "/api/patient"})
public class PatientController {
    private final PatientService patientService;
    private final kigali.clinic.rw.service.AppointmentService appointmentService;

    public PatientController(PatientService patientService, kigali.clinic.rw.service.AppointmentService appointmentService) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Patient> findAll() {
        return patientService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> findById(@PathVariable UUID id) {
        return patientService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping({"", "/save"})
    public ResponseEntity<Patient> create(@RequestBody PatientRequest request) {
        Patient created = patientService.create(request.firstName(), request.lastName(), request.dateOfBirth(), request.ssNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable UUID id, @RequestBody PatientRequest request) {
        return patientService.update(id, request.firstName(), request.lastName(), request.dateOfBirth(), request.ssNumber())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return patientService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // A1: GET /api/patients/by-last-name?lastName=uwase
    @GetMapping("/by-last-name")
    public List<Patient> findByLastName(@RequestParam String lastName) {
        return patientService.findByLastName(lastName);
    }

    // B4: GET /api/patients/of-doctor/{doctorId}
    @GetMapping("/of-doctor/{doctorId}")
    public ResponseEntity<?> findPatientsByDoctor(@PathVariable UUID doctorId) {
        Optional<List<Patient>> result = patientService.findPatientsByDoctor(doctorId);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The doctor with that id does not exist");
        }
        return ResponseEntity.ok(result.get());
    }

    // D2: GET /api/patients/{ssNumber}/upcoming
    @GetMapping("/{ssNumber}/upcoming")
    public ResponseEntity<?> getUpcomingAppointments(
            @PathVariable String ssNumber) {
        Optional<List<kigali.clinic.rw.domain.Appointment>> upcoming = appointmentService.getPatientUpcoming(ssNumber);
        if (upcoming.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("That patient with that ssnumber does not exist");
        }
        return ResponseEntity.ok(upcoming.get());
    }

    public record PatientRequest(String firstName, String lastName, LocalDate dateOfBirth, String ssNumber) { }
}