package com.example.EnlazadosTW.dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO resumido del profesional para respuestas de solicitudes de contacto del tutor.
 */
public record ProfessionalContactSummaryDto(
	UUID professionalProfileId,
	UUID userId,
	String firstName,
	String lastName,
	String specialty,
	List<String> acceptedHealthInsurances,
	BigDecimal sessionFee
) {}
