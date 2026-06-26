package com.example.EnlazadosTW.dtos;

import com.example.EnlazadosTW.enums.DailyReportPriority;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un reporte diario.
 */
public record DailyReportResponseDto(
	UUID id,
	PatientResponseDto patient,
	UserBasicDto author,
	String content,
	DailyReportPriority priority,
	Integer sentimentScore,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
