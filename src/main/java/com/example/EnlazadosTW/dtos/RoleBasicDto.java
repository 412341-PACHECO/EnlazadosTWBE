package com.example.EnlazadosTW.dtos;

import java.util.UUID;

/**
 * DTO básico de un rol, usado en las respuestas de usuario.
 * Contiene solo información esencial del rol.
 */
public record RoleBasicDto(
	UUID id,
	String name
) {}
