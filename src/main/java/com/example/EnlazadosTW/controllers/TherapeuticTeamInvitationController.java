package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.TherapeuticTeamInvitationAcceptDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamInvitationCreateDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamInvitationResponseDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamResponseDto;
import com.example.EnlazadosTW.services.TherapeuticTeamInvitationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador autenticado para invitaciones a equipos terapeuticos.
 */
@RestController
@RequestMapping("/api/therapeutic-team-invitations")
public class TherapeuticTeamInvitationController {

	private final TherapeuticTeamInvitationService invitationService;

	public TherapeuticTeamInvitationController(TherapeuticTeamInvitationService invitationService) {
		this.invitationService = invitationService;
	}

	@PostMapping("/patients/{patientId}")
	public ResponseEntity<TherapeuticTeamInvitationResponseDto> createInvitation(
		@PathVariable UUID patientId,
		@Valid @RequestBody TherapeuticTeamInvitationCreateDto createDto
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.createInvitation(patientId, createDto));
	}

	@GetMapping("/patients/{patientId}")
	public ResponseEntity<List<TherapeuticTeamInvitationResponseDto>> getInvitationsByPatient(@PathVariable UUID patientId) {
		return ResponseEntity.ok(invitationService.getInvitationsByPatient(patientId));
	}

	@PostMapping("/accept")
	public ResponseEntity<TherapeuticTeamResponseDto> acceptInvitation(
		@Valid @RequestBody TherapeuticTeamInvitationAcceptDto acceptDto
	) {
		return ResponseEntity.ok(invitationService.acceptInvitation(acceptDto.token()));
	}

	@DeleteMapping("/{invitationId}")
	public ResponseEntity<Void> cancelInvitation(@PathVariable UUID invitationId) {
		invitationService.cancelInvitation(invitationId);
		return ResponseEntity.noContent().build();
	}
}
