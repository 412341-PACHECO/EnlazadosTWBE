package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.InstitutionCreateDto;
import com.example.EnlazadosTW.dtos.InstitutionMapResponseDto;
import com.example.EnlazadosTW.dtos.InstitutionResponseDto;
import com.example.EnlazadosTW.dtos.InstitutionUpdateDto;
import com.example.EnlazadosTW.services.InstitutionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
 * Controlador REST para gestionar instituciones.
 * Expone endpoints de alta, baja, modificacion y busqueda.
 */
@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

	private final InstitutionService institutionService;

	public InstitutionController(InstitutionService institutionService) {
		this.institutionService = institutionService;
	}

	@PostMapping
	public ResponseEntity<InstitutionResponseDto> createInstitution(
		@Valid @RequestBody InstitutionCreateDto createDto
	) {
		InstitutionResponseDto institution = institutionService.createInstitution(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(institution);
	}

	@GetMapping("/{id}")
	public ResponseEntity<InstitutionResponseDto> getInstitutionById(@PathVariable UUID id) {
		InstitutionResponseDto institution = institutionService.getInstitutionById(id);
		return ResponseEntity.ok(institution);
	}

	@GetMapping
	public ResponseEntity<List<InstitutionResponseDto>> getAllInstitutions() {
		List<InstitutionResponseDto> institutions = institutionService.getAllInstitutions();
		return ResponseEntity.ok(institutions);
	}

	@GetMapping("/search/by-type")
	public ResponseEntity<List<InstitutionResponseDto>> getInstitutionsByType(@RequestParam String type) {
		List<InstitutionResponseDto> institutions = institutionService.getInstitutionsByType(type);
		return ResponseEntity.ok(institutions);
	}

	@GetMapping("/search/by-name")
	public ResponseEntity<List<InstitutionResponseDto>> searchInstitutionsByName(@RequestParam String name) {
		List<InstitutionResponseDto> institutions = institutionService.searchInstitutionsByName(name);
		return ResponseEntity.ok(institutions);
	}

	@GetMapping("/map/nearby")
	public ResponseEntity<List<InstitutionMapResponseDto>> getNearbyInstitutions(
		@RequestParam BigDecimal latitude,
		@RequestParam BigDecimal longitude,
		@RequestParam BigDecimal radiusKm,
		@RequestParam(required = false) String type
	) {
		List<InstitutionMapResponseDto> institutions = institutionService.getNearbyInstitutions(
			latitude,
			longitude,
			radiusKm,
			type
		);
		return ResponseEntity.ok(institutions);
	}

	@PutMapping("/{id}")
	public ResponseEntity<InstitutionResponseDto> updateInstitution(
		@PathVariable UUID id,
		@Valid @RequestBody InstitutionUpdateDto updateDto
	) {
		InstitutionResponseDto institution = institutionService.updateInstitution(id, updateDto);
		return ResponseEntity.ok(institution);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteInstitution(@PathVariable UUID id) {
		institutionService.deleteInstitution(id);
		return ResponseEntity.noContent().build();
	}
}
