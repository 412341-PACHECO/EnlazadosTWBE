package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.ProfessionalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad ProfessionalProfile.
 * Proporciona operaciones CRUD y métodos personalizados para consultas específicas.
 */
@Repository
public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, UUID> {

	/**
	 * Busca un perfil profesional por el ID del usuario.
	 * 
	 * @param userId el ID del usuario
	 * @return Optional con el perfil si existe, o vacío si no existe
	 */
	Optional<ProfessionalProfile> findByUserId(UUID userId);

	/**
	 * Busca todos los perfiles profesionales de una especialidad.
	 * 
	 * @param specialty la especialidad a buscar
	 * @return lista de perfiles profesionales con esa especialidad
	 */
	List<ProfessionalProfile> findBySpecialty(String specialty);

	List<ProfessionalProfile> findBySpecialtyIgnoreCase(String specialty);

	/**
	 * Busca un perfil profesional por número de matrícula.
	 * 
	 * @param licenseNumber el número de matrícula
	 * @return Optional con el perfil si existe, o vacío si no existe
	 */
	Optional<ProfessionalProfile> findByLicenseNumber(String licenseNumber);

	/**
	 * Verifica si existe un perfil profesional para un usuario específico.
	 * 
	 * @param userId el ID del usuario
	 * @return true si existe, false en caso contrario
	 */
	boolean existsByUserId(UUID userId);

	/**
	 * Verifica si existe un número de matrícula.
	 * 
	 * @param licenseNumber el número de matrícula
	 * @return true si existe, false en caso contrario
	 */
	boolean existsByLicenseNumber(String licenseNumber);

}
