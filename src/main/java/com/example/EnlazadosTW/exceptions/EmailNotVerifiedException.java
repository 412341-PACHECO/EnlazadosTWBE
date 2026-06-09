package com.example.EnlazadosTW.exceptions;

public class EmailNotVerifiedException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public EmailNotVerifiedException(String message) {
		super(message);
	}
}
