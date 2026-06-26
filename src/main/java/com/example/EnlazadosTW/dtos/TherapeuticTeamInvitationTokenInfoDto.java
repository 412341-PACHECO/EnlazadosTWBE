package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.TherapeuticTeamInvitationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO publico para inspeccionar una invitacion a partir de su token.
 */
public record TherapeuticTeamInvitationTokenInfoDto(
	String invitedEmail,
	String invitedByFullName,
	String patientFullName,
	TherapeuticTeamInvitationStatus status,
	LocalDate startDate,
	LocalDate endDate,
	LocalDateTime expiresAt,
	boolean userAlreadyRegistered,
	boolean professionalProfileAlreadyCreated
) {}
