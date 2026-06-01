package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO para crear un nuevo perfil profesional.
 */
public record ProfessionalProfileCreateDto(
	@NotNull(message = "El ID del usuario es requerido")
	UUID userId,

	@NotBlank(message = "La especialidad es requerida")
	@Size(min = 3, max = 100, message = "La especialidad debe tener entre 3 y 100 caracteres")
	String specialty,

	@NotBlank(message = "El número de matrícula es requerido")
	@Size(min = 5, max = 50, message = "El número de matrícula debe tener entre 5 y 50 caracteres")
	String licenseNumber,

	@NotNull(message = "La latitud es requerida")
	@DecimalMin(value = "-90.00000000", message = "La latitud debe estar entre -90 y 90") // Min value for latitude
	@DecimalMax(value = "90.00000000", message = "La latitud debe estar entre -90 y 90")   // Max value for latitude
	BigDecimal latitude,

	@NotNull(message = "La longitud es requerida")
	@DecimalMin(value = "-180.00000000", message = "La longitud debe estar entre -180 y 180") // Min value for longitude
	@DecimalMax(value = "180.00000000", message = "La longitud debe estar entre -180 y 180") // Max value for longitude
	BigDecimal longitude,

	List<String> acceptedHealthInsurances,

	@NotNull(message = "El honorario de sesión es requerido")
	@DecimalMin(value = "0.01", message = "El honorario debe ser mayor a 0")
	BigDecimal sessionFee
) {}
