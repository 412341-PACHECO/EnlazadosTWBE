package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.RoleCreateDto;
import com.example.EnlazadosTW.dtos.RoleResponseDto;
import com.example.EnlazadosTW.dtos.RoleUpdateDto;
import com.example.EnlazadosTW.services.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestionar roles del sistema.
 * Endpoints para crear, actualizar, buscar y listar roles.
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

	private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

	/**
	 * Crea un nuevo rol.
	 *
	 * @param createDto datos del rol a crear
	 * @return rol creado
	 */
	@PostMapping
	public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleCreateDto createDto) {
		RoleResponseDto role = roleService.createRole(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(role);
	}

	/**
	 * Obtiene un rol por su ID.
	 *
	 * @param id ID del rol
	 * @return rol encontrado
	 */
	@GetMapping("/{id}")
	public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable UUID id) {
		RoleResponseDto role = roleService.getRoleById(id);
		return ResponseEntity.ok(role);
	}

	/**
	 * Obtiene un rol por su nombre.
	 *
	 * @param name nombre del rol
	 * @return rol encontrado
	 */
	@GetMapping("/search/by-name")
	public ResponseEntity<RoleResponseDto> getRoleByName(@RequestParam String name) {
		RoleResponseDto role = roleService.getRoleByName(name);
		return ResponseEntity.ok(role);
	}

	/**
	 * Obtiene todos los roles del sistema.
	 *
	 * @return lista de todos los roles
	 */
	@GetMapping
	public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
		List<RoleResponseDto> roles = roleService.getAllRoles();
		return ResponseEntity.ok(roles);
	}

	/**
	 * Actualiza un rol existente.
	 *
	 * @param id ID del rol a actualizar
	 * @param updateDto datos a actualizar
	 * @return rol actualizado
	 */
	@PutMapping("/{id}")
	public ResponseEntity<RoleResponseDto> updateRole(
		@PathVariable UUID id,
		@Valid @RequestBody RoleUpdateDto updateDto
	) {
		RoleResponseDto role = roleService.updateRole(id, updateDto);
		return ResponseEntity.ok(role);
	}

	/**
	 * Elimina un rol.
	 *
	 * @param id ID del rol a eliminar
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
		roleService.deleteRole(id);
		return ResponseEntity.noContent().build();
	}
}
