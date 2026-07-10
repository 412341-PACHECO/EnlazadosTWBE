package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

	@NotBlank(message = "El numero de matricula es requerido")
	@Size(min = 5, max = 50, message = "El numero de matricula debe tener entre 5 y 50 caracteres")
	String licenseNumber,

	@NotNull(message = "La latitud es requerida")
	@DecimalMin(value = "-90.00000000", message = "La latitud debe estar entre -90 y 90")
	@DecimalMax(value = "90.00000000", message = "La latitud debe estar entre -90 y 90")
	BigDecimal latitude,

	@NotNull(message = "La longitud es requerida")
	@DecimalMin(value = "-180.00000000", message = "La longitud debe estar entre -180 y 180")
	@DecimalMax(value = "180.00000000", message = "La longitud debe estar entre -180 y 180")
	BigDecimal longitude,

	List<String> acceptedHealthInsurances,

	@NotNull(message = "El honorario de sesion es requerido")
	@DecimalMin(value = "0.01", message = "El honorario debe ser mayor a 0")
	BigDecimal sessionFee,

	@DecimalMin(value = "0.01", message = "El radio de cobertura debe ser mayor a 0")
	BigDecimal coverageRadiusKm
) {}
