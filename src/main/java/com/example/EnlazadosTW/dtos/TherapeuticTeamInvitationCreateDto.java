package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO para crear una invitacion a un equipo terapeutico.
 */
public record TherapeuticTeamInvitationCreateDto(
	@NotBlank(message = "El email del profesional es requerido")
	@Email(message = "El email del profesional debe ser valido")
	String invitedEmail,

	@NotNull(message = "La fecha de inicio es requerida")
	LocalDate startDate,

	LocalDate endDate
) {}
