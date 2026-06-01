package com.example.EnlazadosTW.exceptions;

/**
 * Se lanza cuando las credenciales (email/contraseña) son inválidas.
 */
public class InvalidCredentialsException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public InvalidCredentialsException(String message) {
		super(message);
	}

	public InvalidCredentialsException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCredentialsException() {
		super("Las credenciales proporcionadas son inválidas");
	}
}
