package com.example.EnlazadosTW.entities;

import com.example.EnlazadosTW.enums.TherapeuticTeamInvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Invitacion pendiente para incorporar un profesional al equipo terapeutico de un paciente.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "therapeutic_team_invitations")
public class TherapeuticTeamInvitation extends BaseEntity {

	@NotNull(message = "El paciente es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@NotBlank(message = "El email invitado es requerido")
	@Email(message = "El email invitado debe ser valido")
	@Column(name = "invited_email", nullable = false, length = 255)
	private String invitedEmail;

	@NotNull(message = "El usuario invitador es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invited_by_user_id", nullable = false)
	private User invitedByUser;

	@NotBlank(message = "El token es requerido")
	@Column(nullable = false, unique = true, length = 255)
	private String token;

	@NotNull(message = "El estado es requerido")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TherapeuticTeamInvitationStatus status;

	@NotNull(message = "La fecha de inicio es requerida")
	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@NotNull(message = "La expiracion es requerida")
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "accepted_at")
	private LocalDateTime acceptedAt;
}
