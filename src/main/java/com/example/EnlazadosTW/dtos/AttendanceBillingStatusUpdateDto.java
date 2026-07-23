package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.AttendanceBillingStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar el estado de pago de una liquidacion.
 */
public record AttendanceBillingStatusUpdateDto(
	@NotNull(message = "El estado de pago es requerido")
	AttendanceBillingStatus paymentStatus
) {}
