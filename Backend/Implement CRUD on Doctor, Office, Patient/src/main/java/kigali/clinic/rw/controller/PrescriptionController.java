package kigali.clinic.rw.controller;

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

import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.service.PrescriptionService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/api/prescriptions", "/api/prescription"})
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    // C1: POST /api/prescriptions/save?appointmentId=...
    @PostMapping({"/save", ""})
    public ResponseEntity<String> savePrescription(
            @RequestBody Prescription prescription,
            @RequestParam UUID appointmentId) {
        String result = prescriptionService.writePrescription(prescription, appointmentId);
        if ("The appointment with that id does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        if ("Prescriptions are only for COMPLETED appointments".equals(result)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // C2: GET /api/prescriptions/appointment/{appointmentId}
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getPrescriptionsByAppointment(@PathVariable UUID appointmentId) {
        Optional<List<Prescription>> result = prescriptionService.getPrescriptionsByAppointment(appointmentId);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The appointment with that id does not exist");
        }
        return ResponseEntity.ok(result.get());
    }

    // C3: GET /api/prescriptions/patient/{ssNumber}
    @GetMapping("/patient/{ssNumber}")
    public List<Prescription> getPrescriptionHistory(@PathVariable String ssNumber) {
        return prescriptionService.getPrescriptionHistory(ssNumber);
    }

    // C4: PUT /api/prescriptions/update/{id}
    @PutMapping({"/update/{id}", "/{id}"})
    public ResponseEntity<String> updatePrescription(@PathVariable UUID id, @RequestBody Prescription prescription) {
        String result = prescriptionService.updatePrescription(id, prescription);
        if ("The prescription with that id does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // C4: DELETE /api/prescriptions/delete/{id}
    @DeleteMapping({"/delete/{id}", "/{id}"})
    public ResponseEntity<String> deletePrescription(@PathVariable UUID id) {
        String result = prescriptionService.deletePrescription(id);
        if ("The prescription with that id does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // Bonus 1: GET /api/prescriptions/stats/top-medicines
    @GetMapping("/stats/top-medicines")
    public List<Object[]> getTopMedicines() {
        return prescriptionService.getTopMedicines();
    }
}
