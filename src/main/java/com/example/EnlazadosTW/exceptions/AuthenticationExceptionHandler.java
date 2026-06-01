package com.example.EnlazadosTW.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para autenticación e inicio de sesión.
 * Intercepta las excepciones lanzadas en los controladores y servicios de autenticación
 * y devuelve respuestas de error estructuradas.
 */
@RestControllerAdvice
public class AuthenticationExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationExceptionHandler.class);

	/**
	 * Maneja InvalidCredentialsException (credenciales inválidas).
	 */
	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(
		InvalidCredentialsException ex,
		HttpServletRequest request
	) {
		logger.warn("Intento de login con credenciales inválidas desde: {}", request.getRemoteAddr());

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			ex.getMessage(),
			"INVALID_CREDENTIALS",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja InvalidTokenException (token JWT inválido o malformado).
	 */
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(
		InvalidTokenException ex,
		HttpServletRequest request
	) {
		logger.warn("Token inválido o malformado detectado desde: {}", request.getRemoteAddr());

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			ex.getMessage(),
			"INVALID_TOKEN",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja TokenExpiredException (token JWT expirado).
	 */
	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<ErrorResponse> handleTokenExpired(
		TokenExpiredException ex,
		HttpServletRequest request
	) {
		logger.warn("Intento de acceso con token expirado desde: {}", request.getRemoteAddr());

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			ex.getMessage(),
			"TOKEN_EXPIRED",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja AuthenticationException general.
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(
		AuthenticationException ex,
		HttpServletRequest request
	) {
		logger.error("Error de autenticación desde: {}", request.getRemoteAddr(), ex);

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			ex.getMessage(),
			"AUTHENTICATION_ERROR",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja ExpiredJwtException (token JWT expirado por JwtException).
	 */
	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<ErrorResponse> handleExpiredJwtException(
		ExpiredJwtException ex,
		HttpServletRequest request
	) {
		logger.warn("Token JWT expirado desde: {}", request.getRemoteAddr());

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			"El token de autenticación ha expirado",
			"TOKEN_EXPIRED",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja MalformedJwtException (token JWT malformado).
	 */
	@ExceptionHandler(MalformedJwtException.class)
	public ResponseEntity<ErrorResponse> handleMalformedJwtException(
		MalformedJwtException ex,
		HttpServletRequest request
	) {
		logger.warn("Token JWT malformado desde: {}", request.getRemoteAddr());

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			"El token proporcionado tiene un formato inválido",
			"MALFORMED_TOKEN",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja SignatureException (firma del token inválida).
	 */
	@ExceptionHandler(SignatureException.class)
	public ResponseEntity<ErrorResponse> handleSignatureException(
		SignatureException ex,
		HttpServletRequest request
	) {
		logger.warn("Firma de token JWT inválida desde: {}", request.getRemoteAddr());

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			"La firma del token es inválida",
			"INVALID_SIGNATURE",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja JwtException general (otras excepciones de JWT).
	 */
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<ErrorResponse> handleJwtException(
		JwtException ex,
		HttpServletRequest request
	) {
		logger.warn("Error en token JWT desde: {}", request.getRemoteAddr(), ex);

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			"Error al procesar el token de autenticación",
			"JWT_ERROR",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja BadCredentialsException (credenciales inválidas de Spring Security).
	 */
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
		BadCredentialsException ex,
		HttpServletRequest request
	) {
		logger.warn("Credenciales inválidas desde: {}", request.getRemoteAddr());

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			"Email o contraseña incorrectos",
			"BAD_CREDENTIALS",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maneja UsernameNotFoundException (usuario no encontrado por Spring Security).
	 */
	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUsernameNotFound(
		UsernameNotFoundException ex,
		HttpServletRequest request
	) {
		logger.warn("Usuario no encontrado desde: {}", request.getRemoteAddr());

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.UNAUTHORIZED.value(),
			"Email o contraseña incorrectos",
			"USER_NOT_FOUND",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Manejador genérico para cualquier excepción no capturada.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(
		Exception ex,
		HttpServletRequest request
	) {
		logger.error("Error inesperado desde: {}", request.getRemoteAddr(), ex);

		ErrorResponse error = ErrorResponse.of(
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			"Error interno del servidor",
			"INTERNAL_SERVER_ERROR",
			request.getRequestURI()
		);

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
