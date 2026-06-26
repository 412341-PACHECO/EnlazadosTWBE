package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.DailyReportPriority;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO para actualizar un reporte diario existente.
 */
public record DailyReportUpdateDto(
	UUID patientId,
	UUID authorId,

	@Size(max = 5000, message = "El contenido debe tener como maximo 5000 caracteres")
	String content,

	DailyReportPriority priority,
	Integer sentimentScore
) {}
