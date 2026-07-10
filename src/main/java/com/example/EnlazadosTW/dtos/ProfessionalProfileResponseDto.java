package com.example.EnlazadosTW.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para un perfil profesional.
 */
public record ProfessionalProfileResponseDto(
	UUID id,
	UserBasicDto user,
	String specialty,
	String licenseNumber,
	BigDecimal latitude,
	BigDecimal longitude,
	List<String> acceptedHealthInsurances,
	BigDecimal sessionFee,
	BigDecimal coverageRadiusKm,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
