package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para aceptar una invitacion a un equipo terapeutico.
 */
public record TherapeuticTeamInvitationAcceptDto(
	@NotBlank(message = "El token es requerido")
	String token
) {}
