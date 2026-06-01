package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.UserCreateDto;
import com.example.EnlazadosTW.dtos.UserResponseDto;
import com.example.EnlazadosTW.dtos.UserUpdateDto;
import com.example.EnlazadosTW.entities.Role;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.repositories.RoleRepository;
import com.example.EnlazadosTW.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar usuarios del sistema.
 * Maneja la lógica de negocio para crear, actualizar, buscar y listar usuarios.
 */
@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository,
					   RoleRepository roleRepository,
					   PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Crea un nuevo usuario.
	 * La contraseña se codifica usando BCrypt.
	 *
	 * @param createDto datos del usuario a crear
	 * @return usuario creado
	 * @throws IllegalArgumentException si el email ya existe o el rol no existe
	 */
	public UserResponseDto createUser(UserCreateDto createDto) {
		// Validar que el email no exista
		if (userRepository.existsByEmail(createDto.email())) {
			throw new IllegalArgumentException("Ya existe un usuario con el email: " + createDto.email());
		}

		// Validar que el rol existe
		Role role = roleRepository.findById(createDto.roleId())
			.orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + createDto.roleId()));

		// Crear usuario con contraseña codificada
		User user = User.builder()
			.email(createDto.email())
			.password(passwordEncoder.encode(createDto.password()))
			.firstName(createDto.firstName())
			.lastName(createDto.lastName())
			.role(role)
			.isActive(true) // Los nuevos usuarios están activos por defecto
			.build();

		User savedUser = userRepository.save(user);
		return mapToResponseDto(savedUser);
	}

	/**
	 * Obtiene un usuario por su ID.
	 *
	 * @param id ID del usuario
	 * @return usuario encontrado
	 * @throws IllegalArgumentException si el usuario no existe
	 */
	@Transactional(readOnly = true)
	public UserResponseDto getUserById(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		return mapToResponseDto(user);
	}

	/**
	 * Obtiene un usuario por su email.
	 *
	 * @param email email del usuario
	 * @return usuario encontrado
	 * @throws IllegalArgumentException si el usuario no existe
	 */
	@Transactional(readOnly = true)
	public UserResponseDto getUserByEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

		return mapToResponseDto(user);
	}

	/**
	 * Obtiene todos los usuarios del sistema.
	 *
	 * @return lista de todos los usuarios
	 */
	@Transactional(readOnly = true)
	public List<UserResponseDto> getAllUsers() {
		return userRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Obtiene todos los usuarios activos.
	 *
	 * @return lista de usuarios activos
	 */
	@Transactional(readOnly = true)
	public List<UserResponseDto> getActiveUsers() {
		return userRepository.findByIsActive(true)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Obtiene todos los usuarios de un rol específico.
	 *
	 * @param roleId ID del rol
	 * @return lista de usuarios del rol
	 */
	@Transactional(readOnly = true)
	public List<UserResponseDto> getUsersByRole(UUID roleId) {
		return userRepository.findByRoleId(roleId)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Actualiza un usuario existente.
	 *
	 * @param id ID del usuario a actualizar
	 * @param updateDto datos a actualizar
	 * @return usuario actualizado
	 * @throws IllegalArgumentException si el usuario no existe o el email ya está en uso
	 */
	public UserResponseDto updateUser(UUID id, UserUpdateDto updateDto) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		// Actualizar email si se proporciona y no está en uso
		if (updateDto.email() != null && !updateDto.email().equals(user.getEmail())) {
			if (userRepository.existsByEmail(updateDto.email())) {
				throw new IllegalArgumentException("Ya existe un usuario con el email: " + updateDto.email());
			}
			user.setEmail(updateDto.email());
		}

		// Actualizar nombre si se proporciona
		if (updateDto.firstName() != null) {
			user.setFirstName(updateDto.firstName());
		}

		// Actualizar apellido si se proporciona
		if (updateDto.lastName() != null) {
			user.setLastName(updateDto.lastName());
		}

		// Actualizar contraseña si se proporciona (debe ser codificada)
		if (updateDto.password() != null) {
			user.setPassword(passwordEncoder.encode(updateDto.password()));
		}

		// Actualizar estado si se proporciona
		if (updateDto.isActive() != null) {
			user.setIsActive(updateDto.isActive());
		}

		User updatedUser = userRepository.save(user);
		return mapToResponseDto(updatedUser);
	}

	/**
	 * Desactiva un usuario (lo marca como inactivo).
	 *
	 * @param id ID del usuario a desactivar
	 * @throws IllegalArgumentException si el usuario no existe
	 */
	public UserResponseDto deactivateUser(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		user.setIsActive(false);
		User deactivatedUser = userRepository.save(user);
		return mapToResponseDto(deactivatedUser);
	}

	/**
	 * Activa un usuario (lo marca como activo).
	 *
	 * @param id ID del usuario a activar
	 * @throws IllegalArgumentException si el usuario no existe
	 */
	public UserResponseDto activateUser(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		user.setIsActive(true);
		User activatedUser = userRepository.save(user);
		return mapToResponseDto(activatedUser);
	}

	/**
	 * Mapea una entidad User a UserResponseDto.
	 */
	private UserResponseDto mapToResponseDto(User user) {
		return new UserResponseDto(
			user.getId(),
			user.getEmail(),
			user.getFirstName(),
			user.getLastName(),
			user.getFcmToken(),
			user.getIsActive(),
			new com.example.EnlazadosTW.dtos.RoleBasicDto(user.getRole().getId(), user.getRole().getName()),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
