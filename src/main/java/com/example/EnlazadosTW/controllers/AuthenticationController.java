package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.AuthResponseDto;
import com.example.EnlazadosTW.dtos.LoginRequestDto;
import com.example.EnlazadosTW.dtos.RefreshTokenRequestDto;
import com.example.EnlazadosTW.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación de usuarios.
 * Maneja endpoints de login y refresco de tokens.
 *
 * Las excepciones lanzadas por este controlador son interceptadas por
 * AuthenticationExceptionHandler que las convierte en respuestas JSON estructuradas.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * Endpoint de login.
	 * Valida credenciales y retorna un JWT access token y refresh token.
	 *
	 * @param loginRequest solicitud con email y contraseña
	 * @return respuesta con tokens JWT
	 *
	 * Excepciones posibles:
	 * - InvalidCredentialsException (401): Credenciales inválidas
	 * - UserNotFoundException (401): Usuario no encontrado
	 * - UserInactiveException (403): Usuario inactivo
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
		AuthResponseDto response = authenticationService.authenticate(loginRequest);
		return ResponseEntity.ok(response);
	}

	/**
	 * Endpoint para refrescar el access token.
	 * Utiliza un refresh token válido para obtener un nuevo access token.
	 *
	 * @param refreshRequest solicitud con el refresh token
	 * @return respuesta con nuevo access token
	 *
	 * Excepciones posibles:
	 * - TokenExpiredException (401): Refresh token ha expirado
	 * - InvalidTokenException (401): Refresh token es inválido o malformado
	 */
	@PostMapping("/refresh")
	public ResponseEntity<AuthResponseDto> refreshToken(
		@Valid @RequestBody RefreshTokenRequestDto refreshRequest
	) {
		AuthResponseDto response = authenticationService.refreshAccessToken(refreshRequest.refreshToken());
		return ResponseEntity.ok(response);
	}

	/**
	 * Endpoint de health check para autenticación.
	 * Valida que el token del usuario actual sea válido.
	 */
	@GetMapping("/me")
	public ResponseEntity<String> getCurrentUser() {
		return ResponseEntity.ok("Autenticación válida");
	}
}
