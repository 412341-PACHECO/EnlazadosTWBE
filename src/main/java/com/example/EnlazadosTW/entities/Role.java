package com.example.EnlazadosTW.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un rol del sistema.
 * Cada usuario debe estar asociado a un rol.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role extends BaseEntity {

	@NotBlank(message = "El nombre del rol es requerido")
	@Column(unique = true, nullable = false, length = 100)
	private String name;

}
