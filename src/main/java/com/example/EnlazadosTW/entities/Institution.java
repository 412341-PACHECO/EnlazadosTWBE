package com.example.EnlazadosTW.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una institución vinculada a pacientes.
 * Se define de forma mínima para soportar la relación referencial desde Patient.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "institutions")
public class Institution extends BaseEntity {

	@Size(max = 255, message = "El nombre debe tener como máximo 255 caracteres")
	@Column(length = 255)
	private String name;

	@Size(max = 50, message = "El tipo debe tener como máximo 50 caracteres")
	@Column(length = 50)
	private String type;

	@Size(max = 255, message = "La dirección debe tener como máximo 255 caracteres")
	@Column(length = 255)
	private String address;

	@DecimalMin(value = "-90.00000000", message = "La latitud debe estar entre -90 y 90")
	@DecimalMax(value = "90.00000000", message = "La latitud debe estar entre -90 y 90")
	@Column(precision = 10, scale = 8)
	private BigDecimal latitude;

	@DecimalMin(value = "-180.00000000", message = "La longitud debe estar entre -180 y 180")
	@DecimalMax(value = "180.00000000", message = "La longitud debe estar entre -180 y 180")
	@Column(precision = 11, scale = 8)
	private BigDecimal longitude;
}
