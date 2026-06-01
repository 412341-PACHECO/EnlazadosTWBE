package com.example.EnlazadosTW.exceptions;

/**
 * Se lanza cuando se intenta autenticar a un usuario que está inactivo.
 */
public class UserInactiveException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public UserInactiveException(String message) {
		super(message);
	}

	public UserInactiveException(String message, Throwable cause) {
		super(message, cause);
	}
}
