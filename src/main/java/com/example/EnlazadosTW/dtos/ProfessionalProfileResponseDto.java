package com.example.EnlazadosTW.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para un perfil profesional.
 * Utilizado en operaciones GET, POST (create) y PUT (update).
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
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
