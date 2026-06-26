package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.TherapeuticTeamInvitationTokenInfoDto;
import com.example.EnlazadosTW.services.TherapeuticTeamInvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador publico para inspeccionar tokens de invitacion.
 */
@RestController
@RequestMapping("/api/public/therapeutic-team-invitations")
public class PublicTherapeuticTeamInvitationController {

	private final TherapeuticTeamInvitationService invitationService;

	public PublicTherapeuticTeamInvitationController(TherapeuticTeamInvitationService invitationService) {
		this.invitationService = invitationService;
	}

	@GetMapping("/validate")
	public ResponseEntity<TherapeuticTeamInvitationTokenInfoDto> validateToken(@RequestParam String token) {
		return ResponseEntity.ok(invitationService.inspectInvitationToken(token));
	}
}
