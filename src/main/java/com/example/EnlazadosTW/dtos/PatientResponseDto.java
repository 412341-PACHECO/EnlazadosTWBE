package com.example.EnlazadosTW.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un paciente.
 */
public record PatientResponseDto(
	UUID id,
	String firstName,
	String lastName,
	String diagnosis,
	UserBasicDto parent,
	InstitutionBasicDto institution,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
