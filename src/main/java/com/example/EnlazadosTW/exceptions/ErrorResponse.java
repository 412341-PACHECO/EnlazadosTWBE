package com.example.EnlazadosTW.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de error en formato estándar.
 */
public record ErrorResponse(
	int status,
	String message,
	String error,
	String path,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime timestamp
) {

	/**
	 * Constructor auxiliar para crear respuestas de error sin path.
	 */
	public static ErrorResponse of(int status, String message, String error) {
		return new ErrorResponse(status, message, error, null, LocalDateTime.now());
	}

	/**
	 * Constructor auxiliar para crear respuestas de error con path.
	 */
	public static ErrorResponse of(int status, String message, String error, String path) {
		return new ErrorResponse(status, message, error, path, LocalDateTime.now());
	}
}
