package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.TherapeuticTeamInvitationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para invitaciones a equipos terapeuticos.
 */
public record TherapeuticTeamInvitationResponseDto(
	UUID id,
	PatientResponseDto patient,
	String invitedEmail,
	UserBasicDto invitedByUser,
	TherapeuticTeamInvitationStatus status,
	LocalDate startDate,
	LocalDate endDate,
	LocalDateTime expiresAt,
	LocalDateTime acceptedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
