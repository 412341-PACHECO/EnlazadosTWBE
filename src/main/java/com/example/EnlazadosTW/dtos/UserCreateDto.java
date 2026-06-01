package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO para crear un nuevo usuario.
 */
public record UserCreateDto(
	@NotBlank(message = "El email es requerido")
	@Email(message = "El email debe ser válido")
	String email,

	@NotBlank(message = "La contraseña es requerida")
	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
	String password,

	@NotBlank(message = "El nombre es requerido")
	@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
	String firstName,

	@NotBlank(message = "El apellido es requerido")
	@Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
	String lastName,

	@NotNull
	UUID roleId
) {}
