package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.TherapeuticTeamCreateDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamResponseDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamUpdateDto;
import com.example.EnlazadosTW.services.TherapeuticTeamService;
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
 * Controlador REST para gestionar equipos terapeuticos.
 */
@RestController
@RequestMapping("/api/therapeutic-teams")
public class TherapeuticTeamController {

	private final TherapeuticTeamService therapeuticTeamService;

	public TherapeuticTeamController(TherapeuticTeamService therapeuticTeamService) {
		this.therapeuticTeamService = therapeuticTeamService;
	}

	@PostMapping
	public ResponseEntity<TherapeuticTeamResponseDto> createTherapeuticTeam(
		@Valid @RequestBody TherapeuticTeamCreateDto createDto
	) {
		TherapeuticTeamResponseDto therapeuticTeam = therapeuticTeamService.createTherapeuticTeam(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(therapeuticTeam);
	}

	@GetMapping("/{id}")
	public ResponseEntity<TherapeuticTeamResponseDto> getTherapeuticTeamById(@PathVariable UUID id) {
		return ResponseEntity.ok(therapeuticTeamService.getTherapeuticTeamById(id));
	}

	@GetMapping
	public ResponseEntity<List<TherapeuticTeamResponseDto>> getAllTherapeuticTeams() {
		return ResponseEntity.ok(therapeuticTeamService.getAllTherapeuticTeams());
	}

	@GetMapping("/search/by-patient")
	public ResponseEntity<List<TherapeuticTeamResponseDto>> getTherapeuticTeamsByPatientId(@RequestParam UUID patientId) {
		return ResponseEntity.ok(therapeuticTeamService.getTherapeuticTeamsByPatientId(patientId));
	}

	@GetMapping("/search/by-professional")
	public ResponseEntity<List<TherapeuticTeamResponseDto>> getTherapeuticTeamsByProfessionalId(
		@RequestParam UUID professionalId
	) {
		return ResponseEntity.ok(therapeuticTeamService.getTherapeuticTeamsByProfessionalId(professionalId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TherapeuticTeamResponseDto> updateTherapeuticTeam(
		@PathVariable UUID id,
		@Valid @RequestBody TherapeuticTeamUpdateDto updateDto
	) {
		return ResponseEntity.ok(therapeuticTeamService.updateTherapeuticTeam(id, updateDto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTherapeuticTeam(@PathVariable UUID id) {
		therapeuticTeamService.deleteTherapeuticTeam(id);
		return ResponseEntity.noContent().build();
	}
}
