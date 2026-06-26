package com.example.EnlazadosTW.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa la participacion de un profesional dentro del equipo terapeutico de un paciente.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "therapeutic_teams")
public class TherapeuticTeam extends BaseEntity {

	@NotNull(message = "El paciente es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@NotNull(message = "El perfil profesional es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "professional_id", nullable = false)
	private ProfessionalProfile professional;

	@NotNull(message = "La fecha de inicio es requerida")
	@jakarta.persistence.Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@jakarta.persistence.Column(name = "end_date")
	private LocalDate endDate;
}
