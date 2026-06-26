package com.example.EnlazadosTW.entities;

import com.example.EnlazadosTW.enums.DailyReportPriority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un reporte diario generado por un profesional sobre un paciente.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "daily_reports")
public class DailyReport extends BaseEntity {

	@NotNull(message = "El paciente es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@NotNull(message = "El autor es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@NotBlank(message = "El contenido es requerido")
	@Size(max = 5000, message = "El contenido debe tener como maximo 5000 caracteres")
	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@NotNull(message = "La prioridad es requerida")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DailyReportPriority priority;

	@Column(name = "sentiment_score")
	private Integer sentimentScore;
}
