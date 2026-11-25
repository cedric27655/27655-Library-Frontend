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

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.service.DoctorService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/api/doctors", "/api/doctor"})
public class DoctorController {
    private final DoctorService doctorService;
    private final kigali.clinic.rw.service.AppointmentService appointmentService;

    public DoctorController(DoctorService doctorService, kigali.clinic.rw.service.AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Doctor> findAll() {
        return doctorService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> findById(@PathVariable UUID id) {
        return doctorService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping({"", "/save"})
    public ResponseEntity<Doctor> create(@RequestBody DoctorRequest request) {
        Doctor created = doctorService.create(request.firstName(), request.lastName(), request.dateOfBirth(), request.officeId(), request.specializationIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> update(@PathVariable UUID id, @RequestBody DoctorRequest request) {
        return doctorService.update(id, request.firstName(), request.lastName(), request.dateOfBirth(), request.officeId(), request.specializationIds())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return doctorService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // A1: PUT /api/doctors/{doctorId}/specializations/{specializationId}
    @PutMapping({"/{doctorId}/specializations/{specializationId}", "/{doctorId}/specialization/{specializationId}"})
    public ResponseEntity<String> addSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId) {
        String result = doctorService.addSpecialization(doctorId, specializationId);
        if ("The doctor with that id does not exist".equals(result) || "That specialization does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        if ("The doctor already has that specialization".equals(result)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // A2: DELETE /api/doctors/{doctorId}/specializations/{specializationId}
    @DeleteMapping({"/{doctorId}/specializations/{specializationId}", "/{doctorId}/specialization/{specializationId}"})
    public ResponseEntity<String> removeSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId) {
        String result = doctorService.removeSpecialization(doctorId, specializationId);
        if ("The doctor with that id does not exist".equals(result) || "That specialization does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        if ("The doctor does not have that specialization".equals(result)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // A4: PATCH /api/doctors/{doctorId}/office?officeNumber=104
    @org.springframework.web.bind.annotation.PatchMapping("/{doctorId}/office")
    public ResponseEntity<String> moveDoctorOffice(@PathVariable UUID doctorId, @RequestParam int officeNumber) {
        String result = doctorService.moveDoctorOffice(doctorId, officeNumber);
        if ("The doctor with that id does not exist".equals(result) || "The office with that office number does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        if ("That office is taken by other doctor".equals(result)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{doctorId}/specializations")
    public ResponseEntity<List<Specialization>> getSpecializations(@PathVariable UUID doctorId) {
        List<Specialization> specs = doctorService.getSpecializations(doctorId);
        return ResponseEntity.ok(specs);
    }

    // B1: GET /api/doctors/by-specialization?name=cardiology
    @GetMapping("/by-specialization")
    public List<Doctor> findBySpecializationName(@RequestParam String name) {
        return doctorService.findBySpecializationName(name);
    }

    // D1: GET /api/doctors/{doctorId}/agenda?date=2026-10-20
    @GetMapping("/{doctorId}/agenda")
    public ResponseEntity<?> getDoctorAgenda(
            @PathVariable UUID doctorId,
            @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        Optional<List<kigali.clinic.rw.domain.Appointment>> agenda = appointmentService.getDoctorAgenda(doctorId, localDate);
        if (agenda.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The doctor with that id does not exist");
        }
        return ResponseEntity.ok(agenda.get());
    }

    // D3: GET /api/doctors/search?name=gis
    @GetMapping("/search")
    public List<Doctor> searchByName(@RequestParam String name) {
        return doctorService.searchByName(name);
    }

    // Bonus 2: GET /api/doctors/workload?start=2026-10-01&end=2026-10-31
    @GetMapping("/workload")
    public List<Object[]> getDoctorWorkload(@RequestParam String start, @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return doctorService.getDoctorWorkload(startDate, endDate);
    }

    public record DoctorRequest(String firstName, String lastName, LocalDate dateOfBirth, UUID officeId, List<UUID> specializationIds) { }
}