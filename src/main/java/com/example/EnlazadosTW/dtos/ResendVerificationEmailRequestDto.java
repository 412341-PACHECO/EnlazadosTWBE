package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationEmailRequestDto(
	@NotBlank(message = "El email es requerido")
	@Email(message = "El email debe ser valido")
	String email
) {}
