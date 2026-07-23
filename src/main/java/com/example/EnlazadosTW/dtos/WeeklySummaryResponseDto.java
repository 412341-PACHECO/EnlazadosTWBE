package com.example.EnlazadosTW.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un resumen semanal del legajo interdisciplinario.
 */
public record WeeklySummaryResponseDto(
	UUID id,
	PatientBasicDto patient,
	LocalDate weekStart,
	LocalDate weekEnd,
	String summaryContent,
	Integer reportsCount,
	LocalDateTime generatedAt,
	String modelName,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
