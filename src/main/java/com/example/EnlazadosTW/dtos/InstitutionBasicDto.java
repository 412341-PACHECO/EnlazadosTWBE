package com.example.EnlazadosTW.dtos;

import java.util.UUID;

/**
 * DTO básico de institución para respuestas de entidades relacionadas.
 */
public record InstitutionBasicDto(
	UUID id,
	String name,
	String type
) {}
