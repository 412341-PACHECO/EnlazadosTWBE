package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.InstitutionCreateDto;
import com.example.EnlazadosTW.dtos.InstitutionMapResponseDto;
import com.example.EnlazadosTW.dtos.InstitutionResponseDto;
import com.example.EnlazadosTW.dtos.InstitutionUpdateDto;
import com.example.EnlazadosTW.entities.Institution;
import com.example.EnlazadosTW.repositories.InstitutionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar instituciones.
 * Maneja la logica de negocio para crear, actualizar, buscar y eliminar instituciones.
 */
@Service
@Transactional
public class InstitutionService {

	private static final double EARTH_RADIUS_KM = 6371.0;

	private final InstitutionRepository institutionRepository;

	public InstitutionService(InstitutionRepository institutionRepository) {
		this.institutionRepository = institutionRepository;
	}

	public InstitutionResponseDto createInstitution(InstitutionCreateDto createDto) {
		Institution institution = Institution.builder()
			.name(createDto.name())
			.type(createDto.type())
			.address(createDto.address())
			.latitude(createDto.latitude())
			.longitude(createDto.longitude())
			.build();

		Institution savedInstitution = institutionRepository.save(institution);
		return mapToResponseDto(savedInstitution);
	}

	@Transactional(readOnly = true)
	public InstitutionResponseDto getInstitutionById(UUID id) {
		Institution institution = institutionRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Institucion no encontrada con ID: " + id));

		return mapToResponseDto(institution);
	}

	@Transactional(readOnly = true)
	public List<InstitutionResponseDto> getAllInstitutions() {
		return institutionRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<InstitutionResponseDto> getInstitutionsByType(String type) {
		return institutionRepository.findByType(type)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<InstitutionResponseDto> searchInstitutionsByName(String name) {
		return institutionRepository.findByNameContainingIgnoreCase(name)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<InstitutionMapResponseDto> getNearbyInstitutions(
		BigDecimal latitude,
		BigDecimal longitude,
		BigDecimal radiusKm,
		String type
	) {
		validateGeoSearchParams(latitude, longitude, radiusKm);

		List<Institution> institutions = type == null || type.isBlank()
			? institutionRepository.findAll()
			: institutionRepository.findByType(type);

		double originLatitude = latitude.doubleValue();
		double originLongitude = longitude.doubleValue();

		return institutions.stream()
			.filter(institution -> institution.getLatitude() != null && institution.getLongitude() != null)
			.map(institution -> {
				BigDecimal distanceKm = calculateDistanceKm(
					originLatitude,
					originLongitude,
					institution.getLatitude().doubleValue(),
					institution.getLongitude().doubleValue()
				);
				return new InstitutionDistanceResult(institution, distanceKm);
			})
			.filter(result -> result.distanceKm().compareTo(radiusKm) <= 0)
			.sorted(Comparator.comparing(InstitutionDistanceResult::distanceKm))
			.map(result -> mapToMapResponseDto(result.institution(), result.distanceKm()))
			.collect(Collectors.toList());
	}

	public InstitutionResponseDto updateInstitution(UUID id, InstitutionUpdateDto updateDto) {
		Institution institution = institutionRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Institucion no encontrada con ID: " + id));

		if (updateDto.name() != null) {
			institution.setName(updateDto.name());
		}

		if (updateDto.type() != null) {
			institution.setType(updateDto.type());
		}

		if (updateDto.address() != null) {
			institution.setAddress(updateDto.address());
		}

		if (updateDto.latitude() != null) {
			institution.setLatitude(updateDto.latitude());
		}

		if (updateDto.longitude() != null) {
			institution.setLongitude(updateDto.longitude());
		}

		Institution updatedInstitution = institutionRepository.save(institution);
		return mapToResponseDto(updatedInstitution);
	}

	public void deleteInstitution(UUID id) {
		Institution institution = institutionRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Institucion no encontrada con ID: " + id));

		institutionRepository.delete(institution);
	}

	private void validateGeoSearchParams(BigDecimal latitude, BigDecimal longitude, BigDecimal radiusKm) {
		if (latitude == null || longitude == null) {
			throw new IllegalArgumentException("La latitud y longitud son requeridas");
		}

		if (radiusKm == null || radiusKm.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("El radio de busqueda debe ser mayor a 0");
		}
	}

	private InstitutionResponseDto mapToResponseDto(Institution institution) {
		return new InstitutionResponseDto(
			institution.getId(),
			institution.getName(),
			institution.getType(),
			institution.getAddress(),
			institution.getLatitude(),
			institution.getLongitude(),
			institution.getCreatedAt(),
			institution.getUpdatedAt()
		);
	}

	private InstitutionMapResponseDto mapToMapResponseDto(Institution institution, BigDecimal distanceKm) {
		return new InstitutionMapResponseDto(
			institution.getId(),
			institution.getName(),
			institution.getType(),
			institution.getAddress(),
			institution.getLatitude(),
			institution.getLongitude(),
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

	private record InstitutionDistanceResult(
		Institution institution,
		BigDecimal distanceKm
	) {}
}
