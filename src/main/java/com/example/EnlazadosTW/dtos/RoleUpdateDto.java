package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar un rol existente.
 */
public record RoleUpdateDto(
	@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
	String name
) {}
