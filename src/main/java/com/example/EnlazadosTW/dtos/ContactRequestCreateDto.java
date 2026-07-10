package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO para crear una solicitud de contacto.
 */
public record ContactRequestCreateDto(
	@NotNull(message = "El ID del profesional es requerido")
	UUID professionalId,

	UUID patientId,

	@NotNull(message = "Debe indicarse si se comparte el email")
	Boolean shareEmail,

	@NotNull(message = "Debe indicarse si se comparte el telefono")
	Boolean sharePhone,

	@Size(max = 50, message = "El telefono debe tener como maximo 50 caracteres")
	String parentPhone,

	@Size(max = 2000, message = "El mensaje debe tener como maximo 2000 caracteres")
	String message
) {}
