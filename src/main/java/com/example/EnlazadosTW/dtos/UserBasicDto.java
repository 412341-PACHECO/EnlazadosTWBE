package com.example.EnlazadosTW.dtos;

import java.util.UUID;

/**
 * DTO básico de un usuario, usado en las respuestas de perfil profesional.
 * Contiene solo información esencial del usuario.
 */
public record UserBasicDto(
	UUID id,
	String email,
	String firstName,
	String lastName
) {}
