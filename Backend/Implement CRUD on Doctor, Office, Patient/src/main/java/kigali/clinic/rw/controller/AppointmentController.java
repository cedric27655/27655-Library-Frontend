package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.service.AppointmentService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/api/appointments", "/api/appointment"})
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // A4: POST /api/appointments/save
    @PostMapping({"", "/save"})
    public ResponseEntity<String> saveAppointment(
            @RequestBody AppointmentRequest request,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID doctorId) {

        UUID pId = patientId != null ? patientId : request.patientId();
        UUID dId = doctorId != null ? doctorId : request.doctorId();

        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(request.appointmentDate() != null ? request.appointmentDate() : LocalDate.now());
        appointment.setReason(request.reason());
        appointment.setStatus(request.status() != null ? request.status() : AppointmentStatus.SCHEDULED);

        String result = appointmentService.saveAppointment(appointment, pId, dId);
        if ("Appointment date cannot be in the past".equals(result)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        if ("Doctor is already booked on that date".equals(result)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        if ("That patient or doctor is not available".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public List<Appointment> findAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> findById(@PathVariable UUID id) {
        return appointmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // A2: GET /api/appointments/by-status?status=SCHEDULED
    @GetMapping("/by-status")
    public List<Appointment> findByStatus(@RequestParam AppointmentStatus status) {
        return appointmentService.findByStatus(status);
    }

    // A3: GET /api/appointments/between?start=2026-10-01&end=2026-10-31
    @GetMapping("/between")
    public List<Appointment> findBetween(@RequestParam String start, @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return appointmentService.findBetween(startDate, endDate);
    }

    // C1: GET /api/appointments/stats/by-status
    @GetMapping("/stats/by-status")
    public List<Object[]> countAppointmentsByStatus() {
        return appointmentService.countAppointmentsByStatus();
    }

    // C4: PATCH /api/appointments/cancel-day?doctorId=...&date=2026-10-20
    @PatchMapping("/cancel-day")
    public ResponseEntity<String> cancelDoctorAppointmentsOnDate(
            @RequestParam UUID doctorId,
            @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        String message = appointmentService.cancelDoctorAppointmentsOnDate(doctorId, localDate);
        return ResponseEntity.ok(message);
    }

    // Bonus 1: GET /api/appointments/page?page=0&size=5&sort=appointmentDate,desc
    @GetMapping("/page")
    public Page<Appointment> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "appointmentDate,asc") String sort) {
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        return appointmentService.findAllPaged(pageable);
    }

    // B2: PATCH /api/appointments/{id}/status?status=COMPLETED
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable UUID id, @RequestParam AppointmentStatus status) {
        String result = appointmentService.updateStatus(id, status);
        if ("The appointment with that id does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        if (result.startsWith("Cannot change status from")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // B3: PATCH /api/appointments/{id}/reschedule?newDate=2026-10-30
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<String> reschedule(@PathVariable UUID id, @RequestParam String newDate) {
        LocalDate parsedDate = LocalDate.parse(newDate);
        String result = appointmentService.reschedule(id, parsedDate);
        if ("The appointment with that id does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        if (result.startsWith("A ") || "Appointment date cannot be in the past".equals(result)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        if ("Doctor is already booked on that date".equals(result)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // C4: DELETE /api/appointments/{id} or /delete/{id}
    @DeleteMapping({"/{id}", "/delete/{id}"})
    public ResponseEntity<String> deleteAppointment(@PathVariable UUID id) {
        String result = appointmentService.deleteAppointment(id);
        if ("The appointment with that id does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        if ("That appointment has prescriptions, delete them first".equals(result)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // Bonus 2: DELETE /api/appointments/cancelled-before?date=2026-10-15
    @DeleteMapping("/cancelled-before")
    public ResponseEntity<String> deleteCancelledBefore(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        String message = appointmentService.deleteCancelledBefore(localDate);
        return ResponseEntity.ok(message);
    }

    public record AppointmentRequest(LocalDate appointmentDate, String reason, AppointmentStatus status, UUID patientId, UUID doctorId) { }
}
