package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

/**
 * DTO para actualizar un usuario existente.
 * Todos los campos son opcionales.
 */
public record UserUpdateDto(
	@Email(message = "El email debe ser válido")
	String email,

	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
	String password,

	@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
	String firstName,

	@Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
	String lastName,

	Boolean isActive
) {}
