package com.example.EnlazadosTW.dtos;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para actualizar una asignacion del equipo terapeutico.
 */
public record TherapeuticTeamUpdateDto(
	UUID patientId,
	UUID professionalId,
	LocalDate startDate,
	LocalDate endDate
) {}
