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

@RestControllerAdvice
public class AuthenticationExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationExceptionHandler.class);

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(
		InvalidCredentialsException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, ex.getMessage(), "INVALID_CREDENTIALS", request);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(
		InvalidTokenException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, ex.getMessage(), "INVALID_TOKEN", request);
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<ErrorResponse> handleTokenExpired(
		TokenExpiredException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, ex.getMessage(), "TOKEN_EXPIRED", request);
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<ErrorResponse> handleEmailNotVerified(
		EmailNotVerifiedException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.FORBIDDEN, ex.getMessage(), "EMAIL_NOT_VERIFIED", request);
	}

	@ExceptionHandler(EmailDeliveryException.class)
	public ResponseEntity<ErrorResponse> handleEmailDelivery(
		EmailDeliveryException ex,
		HttpServletRequest request
	) {
		logger.error("Fallo el envio de email", ex);
		return response(HttpStatus.BAD_GATEWAY, ex.getMessage(), "EMAIL_DELIVERY_ERROR", request);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(
		AuthenticationException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, ex.getMessage(), "AUTHENTICATION_ERROR", request);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(
		IllegalArgumentException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.BAD_REQUEST, ex.getMessage(), "BAD_REQUEST", request);
	}

	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<ErrorResponse> handleExpiredJwtException(
		ExpiredJwtException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, "El token de autenticacion ha expirado", "TOKEN_EXPIRED", request);
	}

	@ExceptionHandler(MalformedJwtException.class)
	public ResponseEntity<ErrorResponse> handleMalformedJwtException(
		MalformedJwtException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, "El token proporcionado tiene un formato invalido", "MALFORMED_TOKEN", request);
	}

	@ExceptionHandler(SignatureException.class)
	public ResponseEntity<ErrorResponse> handleSignatureException(
		SignatureException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, "La firma del token es invalida", "INVALID_SIGNATURE", request);
	}

	@ExceptionHandler(JwtException.class)
	public ResponseEntity<ErrorResponse> handleJwtException(
		JwtException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, "Error al procesar el token de autenticacion", "JWT_ERROR", request);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
		BadCredentialsException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, "Email o contrasena incorrectos", "BAD_CREDENTIALS", request);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUsernameNotFound(
		UsernameNotFoundException ex,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNAUTHORIZED, "Email o contrasena incorrectos", "USER_NOT_FOUND", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(
		Exception ex,
		HttpServletRequest request
	) {
		logger.error("Error inesperado", ex);
		return response(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", "INTERNAL_SERVER_ERROR", request);
	}

	private ResponseEntity<ErrorResponse> response(
		HttpStatus status,
		String message,
		String error,
		HttpServletRequest request
	) {
		ErrorResponse body = ErrorResponse.of(status.value(), message, error, request.getRequestURI());
		return new ResponseEntity<>(body, status);
	}
}
