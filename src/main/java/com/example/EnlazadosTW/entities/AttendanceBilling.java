package com.example.EnlazadosTW.entities;

import com.example.EnlazadosTW.enums.AttendanceBillingStatus;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cabecera consolidada de facturacion/asistencias para un periodo mensual.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendance_billings")
public class AttendanceBilling extends BaseEntity {

	@NotNull(message = "El profesional es requerido")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "professional_id", nullable = false)
	private ProfessionalProfile professionalProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@NotBlank(message = "El periodo de facturacion es requerido")
	@Size(max = 7, message = "El periodo de facturacion debe tener formato YYYY-MM")
	@Column(name = "billing_period", nullable = false, length = 7)
	private String billingPeriod;

	@Size(max = 150, message = "La obra social debe tener como maximo 150 caracteres")
	@Column(name = "health_insurance_name", length = 150)
	private String healthInsuranceName;

	@NotNull(message = "La cantidad total de sesiones es requerida")
	@Column(name = "total_sessions", nullable = false)
	private Integer totalSessions;

	@NotNull(message = "El monto total es requerido")
	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@NotNull(message = "El estado de pago es requerido")
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false, length = 20)
	private AttendanceBillingStatus paymentStatus;

	@NotBlank(message = "El hash digital es requerido")
	@Size(max = 64, message = "El hash digital debe tener como maximo 64 caracteres")
	@Column(name = "digital_hash", nullable = false, length = 64, unique = true)
	private String digitalHash;
}
