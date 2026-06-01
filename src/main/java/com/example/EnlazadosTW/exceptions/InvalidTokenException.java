package com.example.EnlazadosTW.exceptions;

/**
 * Se lanza cuando el token JWT es inválido, malformado o ha sido manipulado.
 */
public class InvalidTokenException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public InvalidTokenException(String message) {
		super(message);
	}

	public InvalidTokenException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTokenException() {
		super("El token proporcionado es inválido o malformado");
	}
}
