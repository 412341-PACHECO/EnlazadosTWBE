package com.example.EnlazadosTW.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un usuario del sistema.
 * Contiene datos de autenticación y autorización.
 * Extiende de BaseEntity para heredar campos de auditoría (createdAt, updatedAt).
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

	@NotBlank(message = "El email es requerido")
	@Email(message = "El email debe ser válido")
	@Column(unique = true, nullable = false)
	private String email;

	@NotBlank(message = "La contraseña es requerida")
	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
	@Column(nullable = false)
	private String password;

	@NotBlank(message = "El nombre es requerido")
	@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
	@Column(nullable = false)
	private String firstName;

	@NotBlank(message = "El apellido es requerido")
	@Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
	@Column(nullable = false)
	private String lastName;

	@NotNull(message = "El rol es requerido")
	@ManyToOne
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@Column(name = "fcm_token")
	private String fcmToken;

	@Builder.Default
	@Column(nullable = false)
	private Boolean isActive = true;

	@Builder.Default
	@Column(nullable = false)
	private Boolean enabled = false;

	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	@OneToOne(mappedBy = "user")
	private ProfessionalProfile professionalProfile;
}

