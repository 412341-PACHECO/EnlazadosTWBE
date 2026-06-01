package com.example.EnlazadosTW.dtos;

import java.util.UUID;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para un usuario.
 * NO expone la contraseña por razones de seguridad.
 * Utilizado en operaciones GET, POST (create) y PUT (update).
 */
public record UserResponseDto(
	UUID id,
	String email,
	String firstName,
	String lastName,
	String fcmToken,
	Boolean isActive,
	RoleBasicDto role,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
