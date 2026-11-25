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

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.service.OfficeService;

@CrossOrigin(origins = "*")
@RestController 
@RequestMapping({"/api/offices", "/api/office"})
public class OfficeController {
  private final OfficeService officeService;

  public OfficeController(OfficeService officeService) {
    this.officeService = officeService;
  }

  @GetMapping
  public List<Office> findAll() {
    return officeService.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Office> findById(@PathVariable UUID id) {
    return officeService.findById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping({"", "/save"})
  public ResponseEntity<Office> create(@RequestBody OfficeRequest request) {
    Office created = officeService.create(request.name(), request.officeNumber());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Office> update(@PathVariable UUID id, @RequestBody OfficeRequest request) {
    return officeService.update(id, request.name(), request.officeNumber())
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return officeService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }

  // A3: GET /api/offices/free
  @GetMapping("/free")
  public List<Office> findFreeOffices() {
    return officeService.findFreeOffices();
  }

  // C3: GET /api/offices/busiest
  @GetMapping("/busiest")
  public ResponseEntity<?> findBusiestOffice() {
    Object result = officeService.findBusiestOffice();
    return ResponseEntity.ok(result);
  }

  public record OfficeRequest(String name, int officeNumber) { }
}
