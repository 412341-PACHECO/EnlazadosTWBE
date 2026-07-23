package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.AttendanceRecordStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para actualizar un registro de asistencia.
 */
public record AttendanceRecordUpdateDto(
	UUID professionalId,
	UUID patientId,
	LocalDate sessionDate,

	@DecimalMin(value = "0.01", message = "El honorario nominal debe ser mayor a 0")
	BigDecimal sessionFeeSnapshot,

	@Size(max = 150, message = "La obra social debe tener como maximo 150 caracteres")
	String healthInsuranceName,

	@DecimalMin(value = "0.00", message = "La cobertura de obra social no puede ser negativa")
	BigDecimal healthInsuranceCoverageAmount,

	@DecimalMin(value = "0.00", message = "El copago no puede ser negativo")
	BigDecimal copaymentAmount,

	@Size(max = 2000, message = "Las notas deben tener como maximo 2000 caracteres")
	String notes,

	AttendanceRecordStatus status
) {}
