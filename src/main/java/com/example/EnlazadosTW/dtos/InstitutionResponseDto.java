package com.example.EnlazadosTW.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para una institución.
 */
public record InstitutionResponseDto(
	UUID id,
	String name,
	String type,
	String address,
	BigDecimal latitude,
	BigDecimal longitude,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
