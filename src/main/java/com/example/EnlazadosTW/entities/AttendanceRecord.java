package com.example.EnlazadosTW.entities;

import com.example.EnlazadosTW.enums.AttendanceRecordStatus;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro administrativo de una sesion/asistencia individual facturable.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendance_records")
public class AttendanceRecord extends BaseEntity {

	@NotNull(message = "El profesional es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "professional_profile_id", nullable = false)
	private ProfessionalProfile professionalProfile;

	@NotNull(message = "El paciente es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@NotNull(message = "La fecha de sesion es requerida")
	@Column(name = "session_date", nullable = false)
	private LocalDate sessionDate;

	@NotNull(message = "El honorario nominal es requerido")
	@Column(name = "session_fee_snapshot", nullable = false, precision = 10, scale = 2)
	private BigDecimal sessionFeeSnapshot;

	@NotBlank(message = "La obra social es requerida")
	@Size(max = 150, message = "La obra social debe tener como maximo 150 caracteres")
	@Column(name = "health_insurance_name", nullable = false, length = 150)
	private String healthInsuranceName;

	@Column(name = "health_insurance_coverage_amount", precision = 10, scale = 2)
	private BigDecimal healthInsuranceCoverageAmount;

	@Column(name = "copayment_amount", precision = 10, scale = 2)
	private BigDecimal copaymentAmount;

	@Size(max = 2000, message = "Las notas deben tener como maximo 2000 caracteres")
	@Column(columnDefinition = "text")
	private String notes;

	@NotNull(message = "El estado es requerido")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AttendanceRecordStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "billing_id")
	private AttendanceBilling attendanceBilling;
}
