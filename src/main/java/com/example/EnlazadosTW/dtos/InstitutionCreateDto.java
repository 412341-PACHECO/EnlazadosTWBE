package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO para crear una nueva institución.
 */
public record InstitutionCreateDto(
	@Size(max = 255, message = "El nombre debe tener como máximo 255 caracteres")
	String name,

	@Size(max = 50, message = "El tipo debe tener como máximo 50 caracteres")
	String type,

	@Size(max = 255, message = "La dirección debe tener como máximo 255 caracteres")
	String address,

	@DecimalMin(value = "-90.00000000", message = "La latitud debe estar entre -90 y 90")
	@DecimalMax(value = "90.00000000", message = "La latitud debe estar entre -90 y 90")
	BigDecimal latitude,

	@DecimalMin(value = "-180.00000000", message = "La longitud debe estar entre -180 y 180")
	@DecimalMax(value = "180.00000000", message = "La longitud debe estar entre -180 y 180")
	BigDecimal longitude
) {}
