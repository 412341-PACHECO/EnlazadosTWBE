package com.example.EnlazadosTW.exceptions;

/**
 * Se lanza cuando no se encuentra un usuario con el email proporcionado.
 */
public class UserNotFoundException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public UserNotFoundException(String message) {
		super(message);
	}

	public UserNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
