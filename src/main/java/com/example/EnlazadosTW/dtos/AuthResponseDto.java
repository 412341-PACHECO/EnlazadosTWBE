package com.example.EnlazadosTW.dtos;

/**
 * DTO para respuesta de autenticación.
 * Contiene el token JWT, refresh token, tipo de token, tiempo de expiración, email del usuario y rol.
 */
public record AuthResponseDto(
	String token,
	String refreshToken,
	String tokenType,
	Long expiresIn,
	String email,
	String role
) {
	public AuthResponseDto(String token, String refreshToken, Long expiresIn, String email, String role) {
		this(token, refreshToken, "Bearer", expiresIn, email, role);
	}
}
