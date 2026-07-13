package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.ContactRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para listar las solicitudes enviadas por un tutor.
 */
public record ParentContactRequestResponseDto(
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
	ProfessionalContactSummaryDto professional,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
