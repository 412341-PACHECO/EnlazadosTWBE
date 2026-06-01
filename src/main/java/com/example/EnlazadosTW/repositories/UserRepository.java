package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad User.
 * Proporciona operaciones CRUD y métodos personalizados para consultas específicas.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	/**
	 * Busca un usuario por su email.
	 * 
	 * @param email el email del usuario
	 * @return Optional con el usuario si existe, o vacío si no existe
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Busca todos los usuarios activos.
	 * 
	 * @param isActive estado del usuario
	 * @return lista de usuarios con el estado especificado
	 */
	List<User> findByIsActive(Boolean isActive);

	/**
	 * Busca todos los usuarios asociados a un rol específico.
	 * 
	 * @param roleId el ID del rol
	 * @return lista de usuarios del rol especificado
	 */
	List<User> findByRoleId(UUID roleId);

	/**
	 * Verifica si existe un usuario con un email específico.
	 * 
	 * @param email el email a verificar
	 * @return true si el email existe, false en caso contrario
	 */
	boolean existsByEmail(String email);

}
