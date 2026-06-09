package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO para crear un nuevo paciente.
 */
public record PatientCreateDto(
	@Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
	String firstName,

	@Size(max = 100, message = "El apellido debe tener como máximo 100 caracteres")
	String lastName,

	String diagnosis,

	UUID parentId,

	UUID institutionId
) {}
