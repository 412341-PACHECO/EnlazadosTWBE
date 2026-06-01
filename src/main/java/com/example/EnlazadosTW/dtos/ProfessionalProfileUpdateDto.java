package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para actualizar un perfil profesional existente.
 * Todos los campos son opcionales.
 */
public record ProfessionalProfileUpdateDto(
	@Size(min = 3, max = 100, message = "La especialidad debe tener entre 3 y 100 caracteres")
	String specialty,

	@Size(min = 5, max = 50, message = "El número de matrícula debe tener entre 5 y 50 caracteres")
	String licenseNumber,

	@DecimalMin(value = "-90.00000000", message = "La latitud debe estar entre -90 y 90")
	@DecimalMax(value = "90.00000000", message = "La latitud debe estar entre -90 y 90")
	BigDecimal latitude,

	@DecimalMin(value = "-180.00000000", message = "La longitud debe estar entre -180 y 180")
	@DecimalMax(value = "180.00000000", message = "La longitud debe estar entre -180 y 180")
	BigDecimal longitude,

	List<String> acceptedHealthInsurances,

	@DecimalMin(value = "0.01", message = "El honorario debe ser mayor a 0")
	BigDecimal sessionFee
) {}
