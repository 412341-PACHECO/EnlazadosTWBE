package com.example.EnlazadosTW.dtos;

import java.util.UUID;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para un rol.
 * Utilizado en operaciones GET, POST (create) y PUT (update).
 */
public record RoleResponseDto(
	UUID id,
	String name,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
