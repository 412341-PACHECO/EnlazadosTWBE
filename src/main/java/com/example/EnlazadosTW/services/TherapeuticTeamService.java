package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.InstitutionBasicDto;
import com.example.EnlazadosTW.dtos.PatientResponseDto;
import com.example.EnlazadosTW.dtos.ProfessionalProfileResponseDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamCreateDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamResponseDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamUpdateDto;
import com.example.EnlazadosTW.dtos.UserBasicDto;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.entities.TherapeuticTeam;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.ProfessionalProfileRepository;
import com.example.EnlazadosTW.repositories.TherapeuticTeamRepository;
import com.example.EnlazadosTW.repositories.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar equipos terapeuticos por paciente.
 */
@Service
@Transactional
public class TherapeuticTeamService {

	private final TherapeuticTeamRepository therapeuticTeamRepository;
	private final PatientRepository patientRepository;
	private final ProfessionalProfileRepository professionalProfileRepository;
	private final UserRepository userRepository;
	private final CurrentUserService currentUserService;

	public TherapeuticTeamService(
		TherapeuticTeamRepository therapeuticTeamRepository,
		PatientRepository patientRepository,
		ProfessionalProfileRepository professionalProfileRepository,
		UserRepository userRepository,
		CurrentUserService currentUserService
	) {
		this.therapeuticTeamRepository = therapeuticTeamRepository;
		this.patientRepository = patientRepository;
		this.professionalProfileRepository = professionalProfileRepository;
		this.userRepository = userRepository;
		this.currentUserService = currentUserService;
	}

	public TherapeuticTeamResponseDto createTherapeuticTeam(TherapeuticTeamCreateDto createDto) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PARENT");

		validateDateRange(createDto.startDate(), createDto.endDate());

		Patient patient = getPatientOwnedByCurrentParent(createDto.patientId(), currentUser);
		ProfessionalProfile professional = getProfessionalById(createDto.professionalId());

		return createTherapeuticTeamFromInvitation(patient, professional, createDto.startDate(), createDto.endDate());
	}

	@Transactional(readOnly = true)
	public TherapeuticTeamResponseDto getTherapeuticTeamById(UUID id) {
		return mapToResponseDto(getTherapeuticTeamEntityById(id));
	}

	@Transactional(readOnly = true)
	public List<TherapeuticTeamResponseDto> getAllTherapeuticTeams() {
		return therapeuticTeamRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<TherapeuticTeamResponseDto> getTherapeuticTeamsByPatientId(UUID patientId) {
		return therapeuticTeamRepository.findByPatientId(patientId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<TherapeuticTeamResponseDto> getTherapeuticTeamsByProfessionalId(UUID professionalId) {
		return therapeuticTeamRepository.findByProfessionalId(professionalId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	public TherapeuticTeamResponseDto updateTherapeuticTeam(UUID id, TherapeuticTeamUpdateDto updateDto) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PARENT");

		TherapeuticTeam therapeuticTeam = getTherapeuticTeamEntityById(id);
		getPatientOwnedByCurrentParent(therapeuticTeam.getPatient().getId(), currentUser);

		Patient patient = therapeuticTeam.getPatient();
		if (updateDto.patientId() != null) {
			patient = getPatientOwnedByCurrentParent(updateDto.patientId(), currentUser);
			therapeuticTeam.setPatient(patient);
		}

		ProfessionalProfile professional = therapeuticTeam.getProfessional();
		if (updateDto.professionalId() != null) {
			professional = getProfessionalById(updateDto.professionalId());
			therapeuticTeam.setProfessional(professional);
		}

		LocalDate startDate = updateDto.startDate() != null ? updateDto.startDate() : therapeuticTeam.getStartDate();
		LocalDate endDate = updateDto.endDate() != null ? updateDto.endDate() : therapeuticTeam.getEndDate();

		validateDateRange(startDate, endDate);
		validateNoDateOverlap(patient.getId(), professional.getId(), startDate, endDate, therapeuticTeam.getId());

		if (updateDto.startDate() != null) {
			therapeuticTeam.setStartDate(updateDto.startDate());
		}

		if (updateDto.endDate() != null || updateDto.startDate() != null) {
			therapeuticTeam.setEndDate(endDate);
		}

		return mapToResponseDto(therapeuticTeamRepository.save(therapeuticTeam));
	}

	public void deleteTherapeuticTeam(UUID id) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PARENT");
		TherapeuticTeam therapeuticTeam = getTherapeuticTeamEntityById(id);
		getPatientOwnedByCurrentParent(therapeuticTeam.getPatient().getId(), currentUser);
		therapeuticTeamRepository.delete(therapeuticTeam);
	}

	public TherapeuticTeamResponseDto createTherapeuticTeamFromInvitation(
		Patient patient,
		ProfessionalProfile professional,
		LocalDate startDate,
		LocalDate endDate
	) {
		validateDateRange(startDate, endDate);
		validateNoDateOverlap(patient.getId(), professional.getId(), startDate, endDate, null);

		TherapeuticTeam therapeuticTeam = TherapeuticTeam.builder()
			.patient(patient)
			.professional(professional)
			.startDate(startDate)
			.endDate(endDate)
			.build();

		return mapToResponseDto(therapeuticTeamRepository.save(therapeuticTeam));
	}

	private Patient getPatientOwnedByCurrentParent(UUID patientId, User currentUser) {
		Patient patient = getPatientById(patientId);
		if (patient.getParent() == null || !patient.getParent().getId().equals(currentUser.getId())) {
			throw new IllegalArgumentException("El tutor autenticado no tiene permisos sobre este paciente");
		}
		return patient;
	}

	private Patient getPatientById(UUID patientId) {
		return patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + patientId));
	}

	private ProfessionalProfile getProfessionalById(UUID professionalId) {
		return professionalProfileRepository.findById(professionalId)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + professionalId));
	}

	private TherapeuticTeam getTherapeuticTeamEntityById(UUID id) {
		return therapeuticTeamRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Equipo terapeutico no encontrado con ID: " + id));
	}

	@Transactional(readOnly = true)
	public boolean isProfessionalAssignedToPatient(UUID patientId, UUID professionalId, LocalDate targetDate) {
		return therapeuticTeamRepository.findByPatientIdAndProfessionalId(patientId, professionalId)
			.stream()
			.anyMatch(team -> isActiveOnDate(team, targetDate));
	}

	@Transactional(readOnly = true)
	public boolean hasProfessionalAnyMembership(UUID patientId, UUID professionalId) {
		return therapeuticTeamRepository.findByPatientIdAndProfessionalId(patientId, professionalId)
			.stream()
			.findAny()
			.isPresent();
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (endDate != null && endDate.isBefore(startDate)) {
			throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
		}
	}

	private void validateNoDateOverlap(
		UUID patientId,
		UUID professionalId,
		LocalDate startDate,
		LocalDate endDate,
		UUID currentTeamId
	) {
		boolean hasOverlap = therapeuticTeamRepository.findByPatientIdAndProfessionalId(patientId, professionalId)
			.stream()
			.filter(existing -> currentTeamId == null || !existing.getId().equals(currentTeamId))
			.anyMatch(existing -> dateRangesOverlap(
				startDate,
				endDate,
				existing.getStartDate(),
				existing.getEndDate()
			));

		if (hasOverlap) {
			throw new IllegalArgumentException("Ya existe una asignacion del profesional para el paciente en ese rango de fechas");
		}
	}

	private boolean dateRangesOverlap(
		LocalDate startA,
		LocalDate endA,
		LocalDate startB,
		LocalDate endB
	) {
		LocalDate effectiveEndA = endA != null ? endA : LocalDate.MAX;
		LocalDate effectiveEndB = endB != null ? endB : LocalDate.MAX;
		return !startA.isAfter(effectiveEndB) && !startB.isAfter(effectiveEndA);
	}

	private boolean isActiveOnDate(TherapeuticTeam team, LocalDate targetDate) {
		boolean startsBeforeOrOnDate = !team.getStartDate().isAfter(targetDate);
		boolean endsAfterOrOnDate = team.getEndDate() == null || !team.getEndDate().isBefore(targetDate);
		return startsBeforeOrOnDate && endsAfterOrOnDate;
	}

	private TherapeuticTeamResponseDto mapToResponseDto(TherapeuticTeam therapeuticTeam) {
		return new TherapeuticTeamResponseDto(
			therapeuticTeam.getId(),
			mapPatientToResponseDto(therapeuticTeam.getPatient()),
			mapProfessionalToResponseDto(therapeuticTeam.getProfessional()),
			therapeuticTeam.getStartDate(),
			therapeuticTeam.getEndDate(),
			therapeuticTeam.getCreatedAt(),
			therapeuticTeam.getUpdatedAt()
		);
	}

	private PatientResponseDto mapPatientToResponseDto(Patient patient) {
		UserBasicDto parentDto = null;
		if (patient.getParent() != null) {
			parentDto = mapUserToBasicDto(patient.getParent());
		}

		InstitutionBasicDto institutionDto = null;
		if (patient.getInstitution() != null) {
			institutionDto = new InstitutionBasicDto(
				patient.getInstitution().getId(),
				patient.getInstitution().getName(),
				patient.getInstitution().getType()
			);
		}

		return new PatientResponseDto(
			patient.getId(),
			patient.getFirstName(),
			patient.getLastName(),
			patient.getDiagnosis(),
			parentDto,
			institutionDto,
			patient.getCreatedAt(),
			patient.getUpdatedAt()
		);
	}

	private ProfessionalProfileResponseDto mapProfessionalToResponseDto(ProfessionalProfile professional) {
		return new ProfessionalProfileResponseDto(
			professional.getId(),
			mapUserToBasicDto(professional.getUser()),
			professional.getSpecialty(),
			professional.getLicenseNumber(),
			professional.getLatitude(),
			professional.getLongitude(),
			professional.getAcceptedHealthInsurances(),
			professional.getSessionFee(),
			professional.getCreatedAt(),
			professional.getUpdatedAt()
		);
	}

	private UserBasicDto mapUserToBasicDto(User user) {
		return new UserBasicDto(
			user.getId(),
			user.getEmail(),
			user.getFirstName(),
			user.getLastName()
		);
	}
}
