package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO para generar una liquidacion consolidada de asistencias.
 */
public record AttendanceBillingGenerateDto(
	@NotNull(message = "El ID del profesional es requerido")
	UUID professionalId,

	@NotBlank(message = "El periodo de facturacion es requerido")
	@Pattern(regexp = "^\\d{4}-\\d{2}$", message = "El periodo de facturacion debe tener formato YYYY-MM")
	String billingPeriod,

	UUID patientId,

	@Size(max = 150, message = "La obra social debe tener como maximo 150 caracteres")
	String healthInsuranceName
) {}
