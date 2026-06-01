package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.RoleCreateDto;
import com.example.EnlazadosTW.dtos.RoleResponseDto;
import com.example.EnlazadosTW.dtos.RoleUpdateDto;
import com.example.EnlazadosTW.entities.Role;
import com.example.EnlazadosTW.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar roles del sistema.
 * Maneja la lógica de negocio para crear, actualizar, buscar y listar roles.
 */
@Service
@Transactional
public class RoleService {

	private final RoleRepository roleRepository;

	public RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	/**
	 * Crea un nuevo rol.
	 *
	 * @param createDto datos del rol a crear
	 * @return rol creado
	 * @throws IllegalArgumentException si ya existe un rol con ese nombre
	 */
	public RoleResponseDto createRole(RoleCreateDto createDto) {
		if (roleRepository.existsByName(createDto.name())) {
			throw new IllegalArgumentException("Ya existe un rol con el nombre: " + createDto.name());
		}

		Role role = Role.builder()
			.name(createDto.name())
			.build();

		Role savedRole = roleRepository.save(role);
		return mapToResponseDto(savedRole);
	}

	/**
	 * Obtiene un rol por su ID.
	 *
	 * @param id ID del rol
	 * @return rol encontrado
	 * @throws IllegalArgumentException si el rol no existe
	 */
	public RoleResponseDto getRoleById(UUID id) {
		Role role = roleRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + id));

		return mapToResponseDto(role);
	}

	/**
	 * Obtiene un rol por su nombre.
	 *
	 * @param name nombre del rol
	 * @return rol encontrado
	 * @throws IllegalArgumentException si el rol no existe
	 */
	public RoleResponseDto getRoleByName(String name) {
		Role role = roleRepository.findByName(name)
			.orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con nombre: " + name));

		return mapToResponseDto(role);
	}

	/**
	 * Obtiene todos los roles del sistema.
	 *
	 * @return lista de todos los roles
	 */
	@Transactional(readOnly = true)
	public List<RoleResponseDto> getAllRoles() {
		return roleRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Actualiza un rol existente.
	 *
	 * @param id ID del rol a actualizar
	 * @param updateDto datos del rol
	 * @return rol actualizado
	 * @throws IllegalArgumentException si el rol no existe o el nombre ya está en uso
	 */
	public RoleResponseDto updateRole(UUID id, RoleUpdateDto updateDto) {
		Role role = roleRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + id));

		if (updateDto.name() != null && !updateDto.name().equals(role.getName())) {
			if (roleRepository.existsByName(updateDto.name())) {
				throw new IllegalArgumentException("Ya existe un rol con el nombre: " + updateDto.name());
			}
			role.setName(updateDto.name());
		}

		Role updatedRole = roleRepository.save(role);
		return mapToResponseDto(updatedRole);
	}

	/**
	 * Elimina un rol por su ID.
	 *
	 * @param id ID del rol a eliminar
	 * @throws IllegalArgumentException si el rol no existe
	 */
	public void deleteRole(UUID id) {
		Role role = roleRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + id));

		roleRepository.delete(role);
	}

	/**
	 * Mapea una entidad Role a RoleResponseDto.
	 */
	private RoleResponseDto mapToResponseDto(Role role) {
		return new RoleResponseDto(
			role.getId(),
			role.getName(),
			role.getCreatedAt(),
			role.getUpdatedAt()
		);
	}
}
