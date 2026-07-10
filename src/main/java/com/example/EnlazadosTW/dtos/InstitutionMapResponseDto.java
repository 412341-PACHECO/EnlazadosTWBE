package com.example.EnlazadosTW.dtos;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para representar instituciones en el mapa.
 */
public record InstitutionMapResponseDto(
	UUID id,
	String name,
	String type,
	String address,
	BigDecimal latitude,
	BigDecimal longitude,
	BigDecimal distanceKm
) {}
