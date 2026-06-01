package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Role.
 * Proporciona operaciones CRUD y métodos personalizados para consultas de roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

	/**
	 * Busca un rol por su nombre.
	 * 
	 * @param name el nombre del rol
	 * @return Optional con el rol si existe, o vacío si no existe
	 */
	Optional<Role> findByName(String name);

	/**
	 * Verifica si existe un rol con un nombre específico.
	 * 
	 * @param name el nombre a verificar
	 * @return true si el rol existe, false en caso contrario
	 */
	boolean existsByName(String name);

}
