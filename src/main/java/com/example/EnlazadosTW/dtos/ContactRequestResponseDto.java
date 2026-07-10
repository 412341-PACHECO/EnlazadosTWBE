package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.ContactRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para solicitudes de contacto.
 */
public record ContactRequestResponseDto(
	UUID id,
	UUID parentUserId,
	UUID professionalId,
	UUID patientId,
	String parentFullName,
	String parentEmail,
	String parentPhone,
	String patientFullName,
	String message,
	ContactRequestStatus status,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
