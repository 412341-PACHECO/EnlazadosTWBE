package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para solicitar la generación manual de un resumen semanal.
 */
public record WeeklySummaryGenerateDto(
	@NotNull(message = "El patientId es obligatorio")
	UUID patientId,
	LocalDate referenceDate
) {}
