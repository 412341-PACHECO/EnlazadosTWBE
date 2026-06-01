package com.example.EnlazadosTW.exceptions;

/**
 * Se lanza cuando el token JWT ha expirado.
 */
public class TokenExpiredException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public TokenExpiredException(String message) {
		super(message);
	}

	public TokenExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

	public TokenExpiredException() {
		super("El token ha expirado. Por favor, inicie sesión nuevamente");
	}
}
