package com.example.EnlazadosTW.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entidad que representa el perfil profesional de un terapeuta o profesional.
 * Contiene informacion extendida como especialidad, matricula, ubicacion y honorarios.
 * Extiende de BaseEntity para heredar campos de auditoria.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "professional_profiles")
public class ProfessionalProfile extends BaseEntity {

	@NotNull(message = "El usuario es requerido")
	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@NotBlank(message = "La especialidad es requerida")
	@Size(min = 3, max = 100, message = "La especialidad debe tener entre 3 y 100 caracteres")
	@Column(nullable = false, length = 100)
	private String specialty;

	@NotBlank(message = "El numero de matricula es requerido")
	@Size(min = 5, max = 50, message = "El numero de matricula debe tener entre 5 y 50 caracteres")
	@Column(name = "license_number", nullable = false, length = 50, unique = true)
	private String licenseNumber;

	@NotNull(message = "La latitud es requerida")
	@Column(nullable = false, precision = 10, scale = 8)
	private BigDecimal latitude;

	@NotNull(message = "La longitud es requerida")
	@Column(nullable = false, precision = 11, scale = 8)
	private BigDecimal longitude;

	@Column(name = "accepted_health_insurances", columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private List<String> acceptedHealthInsurances;

	@NotNull(message = "El honorario de sesion es requerido")
	@Column(name = "session_fee", nullable = false, precision = 10, scale = 2)
	private BigDecimal sessionFee;

	@Column(name = "coverage_radius_km", precision = 10, scale = 2)
	private BigDecimal coverageRadiusKm;
}
