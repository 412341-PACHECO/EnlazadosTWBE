package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear un nuevo rol.
 */
public record RoleCreateDto(
	@NotBlank(message = "El nombre del rol es requerido")
	@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
	String name
) {}
