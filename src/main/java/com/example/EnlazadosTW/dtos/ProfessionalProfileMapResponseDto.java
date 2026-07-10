package com.example.EnlazadosTW.dtos;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para representar perfiles profesionales en el mapa.
 */
public record ProfessionalProfileMapResponseDto(
	UUID id,
	UserBasicDto user,
	String specialty,
	String licenseNumber,
	BigDecimal latitude,
	BigDecimal longitude,
	BigDecimal coverageRadiusKm,
	BigDecimal distanceKm
) {}
