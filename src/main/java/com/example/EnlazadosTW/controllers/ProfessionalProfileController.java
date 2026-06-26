package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.ProfessionalProfileCreateDto;
import com.example.EnlazadosTW.dtos.ProfessionalProfileResponseDto;
import com.example.EnlazadosTW.dtos.ProfessionalProfileUpdateDto;
import com.example.EnlazadosTW.services.ProfessionalProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestionar perfiles profesionales.
 * Endpoints para crear, actualizar, buscar y listar perfiles de profesionales.
 */
@RestController
@RequestMapping("/api/professional-profiles")
public class ProfessionalProfileController {

	private final ProfessionalProfileService profileService;

	public ProfessionalProfileController(ProfessionalProfileService profileService) {
		this.profileService = profileService;
	}

	/**
	 * Crea un nuevo perfil profesional.
	 *
	 * @param createDto datos del perfil a crear
	 * @return perfil creado
	 */
	@PostMapping
	public ResponseEntity<ProfessionalProfileResponseDto> createProfessionalProfile(
		@Valid @RequestBody ProfessionalProfileCreateDto createDto
	) {
		ProfessionalProfileResponseDto profile = profileService.createProfessionalProfile(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(profile);
	}

	/**
	 * Obtiene un perfil profesional por su ID.
	 *
	 * @param id ID del perfil
	 * @return perfil encontrado
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ProfessionalProfileResponseDto> getProfileById(@PathVariable UUID id) {
		ProfessionalProfileResponseDto profile = profileService.getProfileById(id);
		return ResponseEntity.ok(profile);
	}

	/**
	 * Obtiene el perfil profesional de un usuario.
	 *
	 * @param userId ID del usuario
	 * @return perfil del usuario
	 */
	@GetMapping("/user/{userId}")
	public ResponseEntity<ProfessionalProfileResponseDto> getProfileByUserId(@PathVariable UUID userId) {
		ProfessionalProfileResponseDto profile = profileService.getProfileByUserId(userId);
		return ResponseEntity.ok(profile);
	}

	@GetMapping("/search/by-user-email")
	public ResponseEntity<ProfessionalProfileResponseDto> getProfileByUserEmail(@RequestParam String email) {
		ProfessionalProfileResponseDto profile = profileService.getProfileByUserEmail(email);
		return ResponseEntity.ok(profile);
	}

	/**
	 * Obtiene todos los perfiles profesionales.
	 *
	 * @return lista de todos los perfiles
	 */
	@GetMapping
	public ResponseEntity<List<ProfessionalProfileResponseDto>> getAllProfiles() {
		List<ProfessionalProfileResponseDto> profiles = profileService.getAllProfiles();
		return ResponseEntity.ok(profiles);
	}

	/**
	 * Obtiene todos los perfiles de una especialidad.
	 *
	 * @param specialty especialidad a buscar
	 * @return lista de perfiles de esa especialidad
	 */
	@GetMapping("/search/by-specialty")
	public ResponseEntity<List<ProfessionalProfileResponseDto>> getProfilesBySpecialty(
		@RequestParam String specialty
	) {
		List<ProfessionalProfileResponseDto> profiles = profileService.getProfilesBySpecialty(specialty);
		return ResponseEntity.ok(profiles);
	}

	/**
	 * Obtiene un perfil profesional por su matrícula.
	 *
	 * @param licenseNumber matrícula a buscar
	 * @return perfil encontrado
	 */
	@GetMapping("/search/by-license")
	public ResponseEntity<ProfessionalProfileResponseDto> getProfileByLicenseNumber(
		@RequestParam String licenseNumber
	) {
		ProfessionalProfileResponseDto profile = profileService.getProfileByLicenseNumber(licenseNumber);
		return ResponseEntity.ok(profile);
	}

	/**
	 * Actualiza un perfil profesional.
	 *
	 * @param id ID del perfil a actualizar
	 * @param updateDto datos a actualizar
	 * @return perfil actualizado
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ProfessionalProfileResponseDto> updateProfessionalProfile(
		@PathVariable UUID id,
		@Valid @RequestBody ProfessionalProfileUpdateDto updateDto
	) {
		ProfessionalProfileResponseDto profile = profileService.updateProfessionalProfile(id, updateDto);
		return ResponseEntity.ok(profile);
	}

	/**
	 * Elimina un perfil profesional.
	 *
	 * @param id ID del perfil a eliminar
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProfessionalProfile(@PathVariable UUID id) {
		profileService.deleteProfessionalProfile(id);
		return ResponseEntity.noContent().build();
	}
}
