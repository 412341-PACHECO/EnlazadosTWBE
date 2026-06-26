package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.ProfessionalProfileCreateDto;
import com.example.EnlazadosTW.dtos.ProfessionalProfileResponseDto;
import com.example.EnlazadosTW.dtos.ProfessionalProfileUpdateDto;
import com.example.EnlazadosTW.dtos.UserBasicDto;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.repositories.ProfessionalProfileRepository;
import com.example.EnlazadosTW.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar perfiles profesionales.
 * Maneja la lógica de negocio para crear, actualizar, buscar y listar perfiles de profesionales.
 */
@Service
@Transactional
public class ProfessionalProfileService {

	private final ProfessionalProfileRepository profileRepository;
	private final UserRepository userRepository;

	public ProfessionalProfileService(ProfessionalProfileRepository profileRepository,
									   UserRepository userRepository) {
		this.profileRepository = profileRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Crea un nuevo perfil profesional para un usuario.
	 *
	 * @param createDto datos del perfil a crear
	 * @return perfil creado
	 * @throws IllegalArgumentException si el usuario no existe o ya tiene perfil profesional
	 */
	public ProfessionalProfileResponseDto createProfessionalProfile(ProfessionalProfileCreateDto createDto) {
		// Validar que el usuario existe
		User user = userRepository.findById(createDto.userId())
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + createDto.userId()));

		// Validar que el usuario no tenga ya un perfil profesional
		if (profileRepository.existsByUserId(createDto.userId())) {
			throw new IllegalArgumentException("El usuario ya tiene un perfil profesional");
		}

		// Validar que la matrícula no esté en uso
		if (profileRepository.existsByLicenseNumber(createDto.licenseNumber())) {
			throw new IllegalArgumentException("Ya existe un perfil con la matrícula: " + createDto.licenseNumber());
		}

		ProfessionalProfile profile = ProfessionalProfile.builder()
			.user(user)
			.specialty(createDto.specialty())
			.licenseNumber(createDto.licenseNumber())
			.latitude(createDto.latitude())
			.longitude(createDto.longitude())
			.acceptedHealthInsurances(createDto.acceptedHealthInsurances())
			.sessionFee(createDto.sessionFee())
			.build();

		ProfessionalProfile savedProfile = profileRepository.save(profile);
		return mapToResponseDto(savedProfile);
	}

	/**
	 * Obtiene un perfil profesional por su ID.
	 *
	 * @param id ID del perfil
	 * @return perfil encontrado
	 * @throws IllegalArgumentException si el perfil no existe
	 */
	@Transactional(readOnly = true)
	public ProfessionalProfileResponseDto getProfileById(UUID id) {
		ProfessionalProfile profile = profileRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + id));

		return mapToResponseDto(profile);
	}

	/**
	 * Obtiene el perfil profesional de un usuario.
	 *
	 * @param userId ID del usuario
	 * @return perfil del usuario
	 * @throws IllegalArgumentException si el usuario no tiene perfil profesional
	 */
	@Transactional(readOnly = true)
	public ProfessionalProfileResponseDto getProfileByUserId(UUID userId) {
		ProfessionalProfile profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new IllegalArgumentException("El usuario no tiene un perfil profesional"));

		return mapToResponseDto(profile);
	}

	/**
	 * Obtiene el perfil profesional a partir del email del usuario.
	 *
	 * @param email email del usuario profesional
	 * @return perfil profesional encontrado
	 */
	@Transactional(readOnly = true)
	public ProfessionalProfileResponseDto getProfileByUserEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

		ProfessionalProfile profile = profileRepository.findByUserId(user.getId())
			.orElseThrow(() -> new IllegalArgumentException("El usuario no tiene un perfil profesional"));

		return mapToResponseDto(profile);
	}

	/**
	 * Obtiene todos los perfiles profesionales.
	 *
	 * @return lista de todos los perfiles
	 */
	@Transactional(readOnly = true)
	public List<ProfessionalProfileResponseDto> getAllProfiles() {
		return profileRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Obtiene todos los perfiles profesionales de una especialidad.
	 *
	 * @param specialty especialidad a buscar
	 * @return lista de perfiles de esa especialidad
	 */
	@Transactional(readOnly = true)
	public List<ProfessionalProfileResponseDto> getProfilesBySpecialty(String specialty) {
		return profileRepository.findBySpecialty(specialty)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Obtiene un perfil profesional por su matrícula.
	 *
	 * @param licenseNumber matrícula a buscar
	 * @return perfil encontrado
	 * @throws IllegalArgumentException si no existe perfil con esa matrícula
	 */
	@Transactional(readOnly = true)
	public ProfessionalProfileResponseDto getProfileByLicenseNumber(String licenseNumber) {
		ProfessionalProfile profile = profileRepository.findByLicenseNumber(licenseNumber)
			.orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con matrícula: " + licenseNumber));

		return mapToResponseDto(profile);
	}

	/**
	 * Actualiza un perfil profesional existente.
	 *
	 * @param id ID del perfil a actualizar
	 * @param updateDto datos a actualizar
	 * @return perfil actualizado
	 * @throws IllegalArgumentException si el perfil no existe o los datos son inválidos
	 */
	public ProfessionalProfileResponseDto updateProfessionalProfile(UUID id, ProfessionalProfileUpdateDto updateDto) {
		ProfessionalProfile profile = profileRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + id));

		// Actualizar especialidad si se proporciona
		if (updateDto.specialty() != null) {
			profile.setSpecialty(updateDto.specialty());
		}

		// Actualizar matrícula si se proporciona y no está en uso
		if (updateDto.licenseNumber() != null && !updateDto.licenseNumber().equals(profile.getLicenseNumber())) {
			if (profileRepository.existsByLicenseNumber(updateDto.licenseNumber())) {
				throw new IllegalArgumentException("Ya existe un perfil con la matrícula: " + updateDto.licenseNumber());
			}
			profile.setLicenseNumber(updateDto.licenseNumber());
		}

		// Actualizar ubicación si se proporciona
		if (updateDto.latitude() != null) {
			profile.setLatitude(updateDto.latitude());
		}
		if (updateDto.longitude() != null) {
			profile.setLongitude(updateDto.longitude());
		}

		// Actualizar seguros aceptados si se proporciona
		if (updateDto.acceptedHealthInsurances() != null) {
			profile.setAcceptedHealthInsurances(updateDto.acceptedHealthInsurances());
		}

		// Actualizar tarifa de sesión si se proporciona
		if (updateDto.sessionFee() != null) {
			profile.setSessionFee(updateDto.sessionFee());
		}

		ProfessionalProfile updatedProfile = profileRepository.save(profile);
		return mapToResponseDto(updatedProfile);
	}

	/**
	 * Elimina un perfil profesional.
	 *
	 * @param id ID del perfil a eliminar
	 * @throws IllegalArgumentException si el perfil no existe
	 */
	public void deleteProfessionalProfile(UUID id) {
		ProfessionalProfile profile = profileRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + id));

		profileRepository.delete(profile);
	}

	/**
	 * Mapea una entidad ProfessionalProfile a ProfessionalProfileResponseDto.
	 */
	private ProfessionalProfileResponseDto mapToResponseDto(ProfessionalProfile profile) {
		UserBasicDto userBasicDto = new UserBasicDto(
			profile.getUser().getId(),
			profile.getUser().getEmail(),
			profile.getUser().getFirstName(),
			profile.getUser().getLastName()
		);

		return new ProfessionalProfileResponseDto(
			profile.getId(),
			userBasicDto,
			profile.getSpecialty(),
			profile.getLicenseNumber(),
			profile.getLatitude(),
			profile.getLongitude(),
			profile.getAcceptedHealthInsurances(),
			profile.getSessionFee(),
			profile.getCreatedAt(),
			profile.getUpdatedAt()
		);
	}
}
