package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.ParentProfileResponseDto;
import com.example.EnlazadosTW.dtos.UserCreateDto;
import com.example.EnlazadosTW.dtos.UserResponseDto;
import com.example.EnlazadosTW.dtos.UserUpdateDto;
import com.example.EnlazadosTW.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestionar usuarios del sistema.
 * Endpoints para crear, actualizar, buscar y listar usuarios.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Crea un nuevo usuario.
	 *
	 * @param createDto datos del usuario a crear
	 * @return usuario creado
	 */
	@PostMapping
	public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto createDto) {
		UserResponseDto user = userService.createUser(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	/**
	 * Obtiene un usuario por su ID.
	 *
	 * @param id ID del usuario
	 * @return usuario encontrado
	 */
	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
		UserResponseDto user = userService.getUserById(id);
		return ResponseEntity.ok(user);
	}

	/**
	 * Obtiene un usuario por su email.
	 *
	 * @param email email del usuario
	 * @return usuario encontrado
	 */
	@GetMapping("/search/by-email")
	public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam String email) {
		UserResponseDto user = userService.getUserByEmail(email);
		return ResponseEntity.ok(user);
	}

	@GetMapping("/parents/{id}")
	public ResponseEntity<ParentProfileResponseDto> getParentProfileById(@PathVariable UUID id) {
		ParentProfileResponseDto parent = userService.getParentProfileById(id);
		return ResponseEntity.ok(parent);
	}

	@GetMapping("/parents/search/by-email")
	public ResponseEntity<ParentProfileResponseDto> getParentProfileByEmail(@RequestParam String email) {
		ParentProfileResponseDto parent = userService.getParentProfileByEmail(email);
		return ResponseEntity.ok(parent);
	}

	/**
	 * Obtiene todos los usuarios del sistema.
	 *
	 * @return lista de todos los usuarios
	 */
	@GetMapping
	public ResponseEntity<List<UserResponseDto>> getAllUsers() {
		List<UserResponseDto> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	/**
	 * Obtiene todos los usuarios activos.
	 *
	 * @return lista de usuarios activos
	 */
	@GetMapping("/active")
	public ResponseEntity<List<UserResponseDto>> getActiveUsers() {
		List<UserResponseDto> users = userService.getActiveUsers();
		return ResponseEntity.ok(users);
	}

	/**
	 * Obtiene todos los usuarios de un rol específico.
	 *
	 * @param roleId ID del rol
	 * @return lista de usuarios del rol
	 */
	@GetMapping("/by-role/{roleId}")
	public ResponseEntity<List<UserResponseDto>> getUsersByRole(@PathVariable UUID roleId) {
		List<UserResponseDto> users = userService.getUsersByRole(roleId);
		return ResponseEntity.ok(users);
	}

	/**
	 * Actualiza un usuario existente.
	 *
	 * @param id ID del usuario a actualizar
	 * @param updateDto datos a actualizar
	 * @return usuario actualizado
	 */
	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDto> updateUser(
		@PathVariable UUID id,
		@Valid @RequestBody UserUpdateDto updateDto
	) {
		UserResponseDto user = userService.updateUser(id, updateDto);
		return ResponseEntity.ok(user);
	}

	/**
	 * Desactiva un usuario.
	 *
	 * @param id ID del usuario a desactivar
	 * @return usuario desactivado
	 */
	@PutMapping("/{id}/deactivate")
	public ResponseEntity<UserResponseDto> deactivateUser(@PathVariable UUID id) {
		UserResponseDto user = userService.deactivateUser(id);
		return ResponseEntity.ok(user);
	}

	/**
	 * Activa un usuario.
	 *
	 * @param id ID del usuario a activar
	 * @return usuario activado
	 */
	@PutMapping("/{id}/activate")
	public ResponseEntity<UserResponseDto> activateUser(@PathVariable UUID id) {
		UserResponseDto user = userService.activateUser(id);
		return ResponseEntity.ok(user);
	}
}
