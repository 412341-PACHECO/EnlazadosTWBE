package com.example.EnlazadosTW.dtos;

import java.util.UUID;

/**
 * DTO basico de paciente para respuestas resumidas.
 */
public record PatientBasicDto(
	UUID id,
	String firstName,
	String lastName,
	String diagnosis
) {}
