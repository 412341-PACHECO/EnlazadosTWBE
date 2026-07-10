package com.example.EnlazadosTW.entities;

import com.example.EnlazadosTW.enums.ContactRequestStatus;
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
 * Solicitud inicial de contacto enviada por un tutor a un profesional.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contact_requests")
public class ContactRequest extends BaseEntity {

	@NotNull(message = "El tutor es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_user_id", nullable = false)
	private User parentUser;

	@NotNull(message = "El profesional es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "professional_profile_id", nullable = false)
	private ProfessionalProfile professionalProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@NotBlank(message = "El nombre del tutor es requerido")
	@Size(max = 200, message = "El nombre del tutor debe tener como maximo 200 caracteres")
	@Column(name = "parent_full_name", nullable = false, length = 200)
	private String parentFullName;

	@Column(name = "parent_email", length = 255)
	private String parentEmail;

	@Column(name = "parent_phone", length = 50)
	private String parentPhone;

	@Size(max = 2000, message = "El mensaje debe tener como maximo 2000 caracteres")
	@Column(columnDefinition = "text")
	private String message;

	@NotNull(message = "El estado es requerido")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ContactRequestStatus status;
}
