package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.ProfessionalProfileCreateDto;
import com.example.EnlazadosTW.dtos.ProfessionalProfileMapResponseDto;
import com.example.EnlazadosTW.dtos.ProfessionalProfileResponseDto;
import com.example.EnlazadosTW.dtos.ProfessionalProfileUpdateDto;
import com.example.EnlazadosTW.dtos.UserBasicDto;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.repositories.ProfessionalProfileRepository;
import com.example.EnlazadosTW.repositories.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar perfiles profesionales.
 * Maneja la logica de negocio para crear, actualizar, buscar y listar perfiles de profesionales.
 */
@Service
@Transactional
public class ProfessionalProfileService {

	private static final double EARTH_RADIUS_KM = 6371.0;
	private static final String THERAPEUTIC_COMPANION_SPECIALTY = "acompanante terapeutico";

	private final ProfessionalProfileRepository profileRepository;
	private final UserRepository userRepository;

	public ProfessionalProfileService(
		ProfessionalProfileRepository profileRepository,
		UserRepository userRepository
	) {
		this.profileRepository = profileRepository;
		this.userRepository = userRepository;
	}

	public ProfessionalProfileResponseDto createProfessionalProfile(ProfessionalProfileCreateDto createDto) {
		User user = userRepository.findById(createDto.userId())
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + createDto.userId()));

		if (profileRepository.existsByUserId(createDto.userId())) {
			throw new IllegalArgumentException("El usuario ya tiene un perfil profesional");
		}

		if (profileRepository.existsByLicenseNumber(createDto.licenseNumber())) {
			throw new IllegalArgumentException("Ya existe un perfil con la matricula: " + createDto.licenseNumber());
		}

		BigDecimal coverageRadiusKm = resolveCoverageRadius(createDto.specialty(), createDto.coverageRadiusKm());

		ProfessionalProfile profile = ProfessionalProfile.builder()
			.user(user)
			.specialty(createDto.specialty())
			.licenseNumber(createDto.licenseNumber())
			.latitude(createDto.latitude())
			.longitude(createDto.longitude())
			.acceptedHealthInsurances(createDto.acceptedHealthInsurances())
			.sessionFee(createDto.sessionFee())
			.coverageRadiusKm(coverageRadiusKm)
			.build();

		ProfessionalProfile savedProfile = profileRepository.save(profile);
		return mapToResponseDto(savedProfile);
	}

	@Transactional(readOnly = true)
	public ProfessionalProfileResponseDto getProfileById(UUID id) {
		ProfessionalProfile profile = profileRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + id));

		return mapToResponseDto(profile);
	}

	@Transactional(readOnly = true)
	public ProfessionalProfileResponseDto getProfileByUserId(UUID userId) {
		ProfessionalProfile profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new IllegalArgumentException("El usuario no tiene un perfil profesional"));

		return mapToResponseDto(profile);
	}

	@Transactional(readOnly = true)
	public ProfessionalProfileResponseDto getProfileByUserEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

		ProfessionalProfile profile = profileRepository.findByUserId(user.getId())
			.orElseThrow(() -> new IllegalArgumentException("El usuario no tiene un perfil profesional"));

		return mapToResponseDto(profile);
	}

	@Transactional(readOnly = true)
	public List<ProfessionalProfileResponseDto> getAllProfiles() {
		return profileRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ProfessionalProfileResponseDto> getProfilesBySpecialty(String specialty) {
		return profileRepository.findBySpecialty(specialty)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ProfessionalProfileMapResponseDto> getNearbyProfiles(
		BigDecimal latitude,
		BigDecimal longitude,
		BigDecimal radiusKm,
		String specialty,
		String acceptedHealthInsurance
	) {
		validateGeoSearchParams(latitude, longitude, radiusKm);

		List<ProfessionalProfile> profiles = specialty == null || specialty.isBlank()
			? profileRepository.findAll()
			: profileRepository.findBySpecialtyIgnoreCase(specialty);

		double originLatitude = latitude.doubleValue();
		double originLongitude = longitude.doubleValue();

		return profiles.stream()
			.filter(profile -> profile.getLatitude() != null && profile.getLongitude() != null)
			.filter(profile -> matchesAcceptedHealthInsurance(profile, acceptedHealthInsurance))
			.map(profile -> {
				BigDecimal distanceKm = calculateDistanceKm(
					originLatitude,
					originLongitude,
					profile.getLatitude().doubleValue(),
					profile.getLongitude().doubleValue()
				);
				return new ProfessionalDistanceResult(profile, distanceKm);
			})
			.filter(result -> isInsideSearchArea(result.profile(), result.distanceKm(), radiusKm))
			.sorted(Comparator.comparing(ProfessionalDistanceResult::distanceKm))
			.map(result -> mapToMapResponseDto(result.profile(), result.distanceKm()))
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ProfessionalProfileResponseDto getProfileByLicenseNumber(String licenseNumber) {
		ProfessionalProfile profile = profileRepository.findByLicenseNumber(licenseNumber)
			.orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado con matricula: " + licenseNumber));

		return mapToResponseDto(profile);
	}

	public ProfessionalProfileResponseDto updateProfessionalProfile(UUID id, ProfessionalProfileUpdateDto updateDto) {
		ProfessionalProfile profile = profileRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + id));

		String finalSpecialty = updateDto.specialty() != null ? updateDto.specialty() : profile.getSpecialty();

		if (updateDto.specialty() != null) {
			profile.setSpecialty(updateDto.specialty());
		}

		if (updateDto.licenseNumber() != null && !updateDto.licenseNumber().equals(profile.getLicenseNumber())) {
			if (profileRepository.existsByLicenseNumber(updateDto.licenseNumber())) {
				throw new IllegalArgumentException("Ya existe un perfil con la matricula: " + updateDto.licenseNumber());
			}
			profile.setLicenseNumber(updateDto.licenseNumber());
		}

		if (updateDto.latitude() != null) {
			profile.setLatitude(updateDto.latitude());
		}
		if (updateDto.longitude() != null) {
			profile.setLongitude(updateDto.longitude());
		}

		if (updateDto.acceptedHealthInsurances() != null) {
			profile.setAcceptedHealthInsurances(updateDto.acceptedHealthInsurances());
		}

		if (updateDto.sessionFee() != null) {
			profile.setSessionFee(updateDto.sessionFee());
		}

		BigDecimal requestedCoverageRadius = updateDto.coverageRadiusKm() != null
			? updateDto.coverageRadiusKm()
			: profile.getCoverageRadiusKm();
		profile.setCoverageRadiusKm(resolveCoverageRadius(finalSpecialty, requestedCoverageRadius));

		ProfessionalProfile updatedProfile = profileRepository.save(profile);
		return mapToResponseDto(updatedProfile);
	}

	public void deleteProfessionalProfile(UUID id) {
		ProfessionalProfile profile = profileRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + id));

		profileRepository.delete(profile);
	}

	private void validateGeoSearchParams(BigDecimal latitude, BigDecimal longitude, BigDecimal radiusKm) {
		if (latitude == null || longitude == null) {
			throw new IllegalArgumentException("La latitud y longitud son requeridas");
		}

		if (radiusKm == null || radiusKm.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("El radio de busqueda debe ser mayor a 0");
		}
	}

	private BigDecimal resolveCoverageRadius(String specialty, BigDecimal coverageRadiusKm) {
		if (isTherapeuticCompanion(specialty)) {
			if (coverageRadiusKm == null || coverageRadiusKm.compareTo(BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException(
					"El radio de cobertura es obligatorio para la especialidad Acompanante Terapeutico"
				);
			}
			return coverageRadiusKm;
		}

		return null;
	}

	private boolean isInsideSearchArea(
		ProfessionalProfile profile,
		BigDecimal distanceKm,
		BigDecimal radiusKm
	) {
		if (isTherapeuticCompanion(profile.getSpecialty())) {
			BigDecimal coverageRadiusKm = profile.getCoverageRadiusKm();
			return coverageRadiusKm != null && distanceKm.compareTo(coverageRadiusKm) <= 0;
		}

		return distanceKm.compareTo(radiusKm) <= 0;
	}

	private boolean matchesAcceptedHealthInsurance(
		ProfessionalProfile profile,
		String acceptedHealthInsurance
	) {
		if (acceptedHealthInsurance == null || acceptedHealthInsurance.isBlank()) {
			return true;
		}

		if (profile.getAcceptedHealthInsurances() == null || profile.getAcceptedHealthInsurances().isEmpty()) {
			return false;
		}

		String normalizedRequestedInsurance = normalizeSearchValue(acceptedHealthInsurance);

		return profile.getAcceptedHealthInsurances()
			.stream()
			.filter(insurance -> insurance != null && !insurance.isBlank())
			.map(this::normalizeSearchValue)
			.anyMatch(normalizedRequestedInsurance::equals);
	}

	private boolean isTherapeuticCompanion(String specialty) {
		if (specialty == null) {
			return false;
		}

		return THERAPEUTIC_COMPANION_SPECIALTY.equals(normalizeSearchValue(specialty));
	}

	private String normalizeSearchValue(String value) {
		return Normalizer.normalize(value, Normalizer.Form.NFD)
			.replaceAll("\\p{M}", "").
			toLowerCase()
			.trim();
	}

	private ProfessionalProfileMapResponseDto mapToMapResponseDto(
		ProfessionalProfile profile,
		BigDecimal distanceKm
	) {
		UserBasicDto userBasicDto = new UserBasicDto(
			profile.getUser().getId(),
			profile.getUser().getEmail(),
			profile.getUser().getFirstName(),
			profile.getUser().getLastName()
		);

		return new ProfessionalProfileMapResponseDto(
			profile.getId(),
			userBasicDto,
			profile.getSpecialty(),
			profile.getLicenseNumber(),
			profile.getLatitude(),
			profile.getLongitude(),
			profile.getCoverageRadiusKm(),
			distanceKm
		);
	}

	private BigDecimal calculateDistanceKm(
		double originLatitude,
		double originLongitude,
		double destinationLatitude,
		double destinationLongitude
	) {
		double latitudeDistance = Math.toRadians(destinationLatitude - originLatitude);
		double longitudeDistance = Math.toRadians(destinationLongitude - originLongitude);
		double a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
			+ Math.cos(Math.toRadians(originLatitude))
			* Math.cos(Math.toRadians(destinationLatitude))
			* Math.sin(longitudeDistance / 2)
			* Math.sin(longitudeDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = EARTH_RADIUS_KM * c;
		return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
	}

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
			profile.getCoverageRadiusKm(),
			profile.getCreatedAt(),
			profile.getUpdatedAt()
		);
	}

	private record ProfessionalDistanceResult(
		ProfessionalProfile profile,
		BigDecimal distanceKm
	) {}
}
