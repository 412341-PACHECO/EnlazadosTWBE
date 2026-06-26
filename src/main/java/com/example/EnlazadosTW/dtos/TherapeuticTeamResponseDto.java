package com.example.EnlazadosTW.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para una asignacion del equipo terapeutico.
 */
public record TherapeuticTeamResponseDto(
	UUID id,
	PatientResponseDto patient,
	ProfessionalProfileResponseDto professional,
	LocalDate startDate,
	LocalDate endDate,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
