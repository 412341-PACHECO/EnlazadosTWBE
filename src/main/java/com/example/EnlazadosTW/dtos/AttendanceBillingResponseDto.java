package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.AttendanceBillingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para liquidaciones administrativas de asistencias.
 */
public record AttendanceBillingResponseDto(
	UUID id,
	ProfessionalContactSummaryDto professional,
	PatientBasicDto patient,
	String billingPeriod,
	String healthInsuranceName,
	Integer totalSessions,
	BigDecimal totalAmount,
	AttendanceBillingStatus paymentStatus,
	String digitalHash,
	List<UUID> attendanceRecordIds,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
