package com.example.EnlazadosTW.exceptions;

public class EmailDeliveryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmailDeliveryException(String message) {
		super(message);
	}

	public EmailDeliveryException(String message, Throwable cause) {
		super(message, cause);
	}
}
