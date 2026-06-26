package com.example.EnlazadosTW.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de perfil de tutor con sus pacientes asociados.
 */
public record ParentProfileResponseDto(
	UUID id,
	String email,
	String firstName,
	String lastName,
	Boolean isActive,
	Boolean enabled,
	RoleBasicDto role,
	List<PatientBasicDto> patients,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
