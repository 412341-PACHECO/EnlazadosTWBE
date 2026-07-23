package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para crear un nuevo registro de asistencia.
 */
public record AttendanceRecordCreateDto(
	@NotNull(message = "El ID del profesional es requerido")
	UUID professionalId,

	@NotNull(message = "El ID del paciente es requerido")
	UUID patientId,

	@NotNull(message = "La fecha de sesion es requerida")
	LocalDate sessionDate,

	@NotNull(message = "El honorario nominal es requerido")
	@DecimalMin(value = "0.01", message = "El honorario nominal debe ser mayor a 0")
	BigDecimal sessionFeeSnapshot,

	@NotBlank(message = "La obra social es requerida")
	@Size(max = 150, message = "La obra social debe tener como maximo 150 caracteres")
	String healthInsuranceName,

	@DecimalMin(value = "0.00", message = "La cobertura de obra social no puede ser negativa")
	BigDecimal healthInsuranceCoverageAmount,

	@DecimalMin(value = "0.00", message = "El copago no puede ser negativo")
	BigDecimal copaymentAmount,

	@Size(max = 2000, message = "Las notas deben tener como maximo 2000 caracteres")
	String notes
) {}
