package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.AttendanceRecordStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para registros de asistencia.
 */
public record AttendanceRecordResponseDto(
	UUID id,
	ProfessionalContactSummaryDto professional,
	PatientBasicDto patient,
	LocalDate sessionDate,
	BigDecimal sessionFeeSnapshot,
	String healthInsuranceName,
	BigDecimal healthInsuranceCoverageAmount,
	BigDecimal copaymentAmount,
	String notes,
	AttendanceRecordStatus status,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
