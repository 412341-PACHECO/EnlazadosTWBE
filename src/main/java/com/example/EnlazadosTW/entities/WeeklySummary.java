package com.example.EnlazadosTW.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resumen semanal generado para el legajo interdisciplinario de un paciente.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
	name = "weekly_summaries",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_weekly_summary_patient_period",
		columnNames = { "patient_id", "week_start", "week_end" }
	)
)
public class WeeklySummary extends BaseEntity {

	@NotNull(message = "El paciente es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@NotNull(message = "La fecha de inicio de semana es requerida")
	@Column(name = "week_start", nullable = false)
	private LocalDate weekStart;

	@NotNull(message = "La fecha de fin de semana es requerida")
	@Column(name = "week_end", nullable = false)
	private LocalDate weekEnd;

	@NotNull(message = "El resumen semanal es requerido")
	@Column(name = "summary_content", nullable = false, columnDefinition = "text")
	private String summaryContent;

	@NotNull(message = "La cantidad de reportes es requerida")
	@Column(name = "reports_count", nullable = false)
	private Integer reportsCount;

	@NotNull(message = "La fecha de generacion es requerida")
	@Column(name = "generated_at", nullable = false)
	private LocalDateTime generatedAt;

	@Column(name = "model_name", length = 100)
	private String modelName;
}
