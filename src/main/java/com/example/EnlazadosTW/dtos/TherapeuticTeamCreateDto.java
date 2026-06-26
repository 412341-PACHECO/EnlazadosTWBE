package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para crear una nueva asignacion dentro del equipo terapeutico.
 */
public record TherapeuticTeamCreateDto(
	@NotNull(message = "El ID del paciente es requerido")
	UUID patientId,

	@NotNull(message = "El ID del perfil profesional es requerido")
	UUID professionalId,

	@NotNull(message = "La fecha de inicio es requerida")
	LocalDate startDate,

	LocalDate endDate
) {}
