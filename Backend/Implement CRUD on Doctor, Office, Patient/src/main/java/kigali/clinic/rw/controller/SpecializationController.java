package kigali.clinic.rw.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.service.SpecializationService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/api/specializations", "/api/specialization"})
public class SpecializationController {
    private final SpecializationService specializationService;

    public SpecializationController(SpecializationService specializationService) {
        this.specializationService = specializationService;
    }

    @GetMapping
    public List<Specialization> findAll() {
        return specializationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Specialization> findById(@PathVariable UUID id) {
        return specializationService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping({"", "/save"})
    public ResponseEntity<Specialization> create(@RequestBody SpecializationRequest request) {
        Specialization created = specializationService.create(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Specialization> update(@PathVariable UUID id, @RequestBody SpecializationRequest request) {
        return specializationService.update(id, request.name())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return specializationService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<Doctor>> getDoctors(@PathVariable UUID id) {
        return ResponseEntity.ok(specializationService.getDoctors(id));
    }

    // B3: GET /api/specializations/unused
    @GetMapping("/unused")
    public List<Specialization> findUnusedSpecializations() {
        return specializationService.findUnusedSpecializations();
    }

    public record SpecializationRequest(String name) { }
}
