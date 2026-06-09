package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.PatientCreateDto;
import com.example.EnlazadosTW.dtos.PatientResponseDto;
import com.example.EnlazadosTW.dtos.PatientUpdateDto;
import com.example.EnlazadosTW.services.PatientService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar pacientes.
 * Expone endpoints básicos de alta, baja y modificación.
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	@PostMapping
	public ResponseEntity<PatientResponseDto> createPatient(@Valid @RequestBody PatientCreateDto createDto) {
		PatientResponseDto patient = patientService.createPatient(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(patient);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PatientResponseDto> getPatientById(@PathVariable UUID id) {
		PatientResponseDto patient = patientService.getPatientById(id);
		return ResponseEntity.ok(patient);
	}

	@GetMapping
	public ResponseEntity<List<PatientResponseDto>> getAllPatients() {
		List<PatientResponseDto> patients = patientService.getAllPatients();
		return ResponseEntity.ok(patients);
	}

	@GetMapping("/search/by-parent")
	public ResponseEntity<List<PatientResponseDto>> getPatientsByParentId(@RequestParam UUID parentId) {
		List<PatientResponseDto> patients = patientService.getPatientsByParentId(parentId);
		return ResponseEntity.ok(patients);
	}

	@GetMapping("/search/by-institution")
	public ResponseEntity<List<PatientResponseDto>> getPatientsByInstitutionId(@RequestParam UUID institutionId) {
		List<PatientResponseDto> patients = patientService.getPatientsByInstitutionId(institutionId);
		return ResponseEntity.ok(patients);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PatientResponseDto> updatePatient(
		@PathVariable UUID id,
		@Valid @RequestBody PatientUpdateDto updateDto
	) {
		PatientResponseDto patient = patientService.updatePatient(id, updateDto);
		return ResponseEntity.ok(patient);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
		patientService.deletePatient(id);
		return ResponseEntity.noContent().build();
	}
}
