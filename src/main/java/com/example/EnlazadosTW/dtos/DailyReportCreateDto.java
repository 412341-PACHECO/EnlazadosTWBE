package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.DailyReportPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO para crear un nuevo reporte diario.
 */
public record DailyReportCreateDto(
	@NotNull(message = "El ID del paciente es requerido")
	UUID patientId,

	@NotNull(message = "El ID del autor es requerido")
	UUID authorId,

	@NotBlank(message = "El contenido es requerido")
	@Size(max = 5000, message = "El contenido debe tener como maximo 5000 caracteres")
	String content,

	@NotNull(message = "La prioridad es requerida")
	DailyReportPriority priority,

	Integer sentimentScore
) {}
