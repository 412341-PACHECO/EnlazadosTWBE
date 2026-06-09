package com.example.EnlazadosTW.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa a un paciente dentro del sistema.
 * Se vincula con el usuario padre/tutor y con una institución.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "patients")
public class Patient extends BaseEntity {

	@Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
	@Column(name = "first_name", length = 100)
	private String firstName;

	@Size(max = 100, message = "El apellido debe tener como máximo 100 caracteres")
	@Column(name = "last_name", length = 100)
	private String lastName;

	@Column(columnDefinition = "text")
	private String diagnosis;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private User parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "institution_id")
	private Institution institution;
}
