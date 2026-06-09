package com.example.EnlazadosTW.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
	@NotBlank(message = "El token es requerido")
	String token,

	@NotBlank(message = "La nueva contrasena es requerida")
	@Size(min = 8, message = "La nueva contrasena debe tener al menos 8 caracteres")
	String newPassword
) {}
