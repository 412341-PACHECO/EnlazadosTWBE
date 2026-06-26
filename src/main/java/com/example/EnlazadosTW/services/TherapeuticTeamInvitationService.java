package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.InstitutionBasicDto;
import com.example.EnlazadosTW.dtos.PatientResponseDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamInvitationCreateDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamInvitationResponseDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamInvitationTokenInfoDto;
import com.example.EnlazadosTW.dtos.TherapeuticTeamResponseDto;
import com.example.EnlazadosTW.dtos.UserBasicDto;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.entities.TherapeuticTeamInvitation;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.enums.TherapeuticTeamInvitationStatus;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.ProfessionalProfileRepository;
import com.example.EnlazadosTW.repositories.TherapeuticTeamInvitationRepository;
import com.example.EnlazadosTW.repositories.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar invitaciones a equipos terapeuticos.
 */
@Service
@Transactional
public class TherapeuticTeamInvitationService {

	private final TherapeuticTeamInvitationRepository invitationRepository;
	private final PatientRepository patientRepository;
	private final UserRepository userRepository;
	private final ProfessionalProfileRepository professionalProfileRepository;
	private final TherapeuticTeamService therapeuticTeamService;
	private final CurrentUserService currentUserService;
	private final EmailService emailService;
	private final long expirationMinutes;

	public TherapeuticTeamInvitationService(
		TherapeuticTeamInvitationRepository invitationRepository,
		PatientRepository patientRepository,
		UserRepository userRepository,
		ProfessionalProfileRepository professionalProfileRepository,
		TherapeuticTeamService therapeuticTeamService,
		CurrentUserService currentUserService,
		EmailService emailService,
		@Value("${app.email.therapeutic-team-invitation-expiration-minutes:10080}") long expirationMinutes
	) {
		this.invitationRepository = invitationRepository;
		this.patientRepository = patientRepository;
		this.userRepository = userRepository;
		this.professionalProfileRepository = professionalProfileRepository;
		this.therapeuticTeamService = therapeuticTeamService;
		this.currentUserService = currentUserService;
		this.emailService = emailService;
		this.expirationMinutes = expirationMinutes;
	}

	public TherapeuticTeamInvitationResponseDto createInvitation(UUID patientId, TherapeuticTeamInvitationCreateDto createDto) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PARENT");

		Patient patient = getPatientOwnedByCurrentParent(patientId, currentUser);
		validateDateRange(createDto.startDate(), createDto.endDate());
		validateNoPendingInvitationOverlap(patientId, createDto.invitedEmail(), createDto.startDate(), createDto.endDate());

		String normalizedEmail = createDto.invitedEmail().trim().toLowerCase();
		String token = UUID.randomUUID().toString();

		TherapeuticTeamInvitation invitation = TherapeuticTeamInvitation.builder()
			.patient(patient)
			.invitedEmail(normalizedEmail)
			.invitedByUser(currentUser)
			.token(token)
			.status(TherapeuticTeamInvitationStatus.PENDING)
			.startDate(createDto.startDate())
			.endDate(createDto.endDate())
			.expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
			.build();

		TherapeuticTeamInvitation savedInvitation = invitationRepository.save(invitation);
		emailService.sendTherapeuticTeamInvitationEmail(savedInvitation);

		return mapToResponseDto(savedInvitation);
	}

	@Transactional(readOnly = true)
	public List<TherapeuticTeamInvitationResponseDto> getInvitationsByPatient(UUID patientId) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PARENT");
		getPatientOwnedByCurrentParent(patientId, currentUser);

		return invitationRepository.findByPatientId(patientId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public TherapeuticTeamInvitationTokenInfoDto inspectInvitationToken(String token) {
		TherapeuticTeamInvitation invitation = getInvitationByToken(token);
		refreshExpiredStatus(invitation);

		User existingUser = userRepository.findByEmail(invitation.getInvitedEmail()).orElse(null);
		boolean userAlreadyRegistered = existingUser != null;
		boolean professionalProfileAlreadyCreated = userAlreadyRegistered
			&& professionalProfileRepository.findByUserId(existingUser.getId()).isPresent();

		return new TherapeuticTeamInvitationTokenInfoDto(
			invitation.getInvitedEmail(),
			invitation.getInvitedByUser().getFirstName() + " " + invitation.getInvitedByUser().getLastName(),
			invitation.getPatient().getFirstName() + " " + invitation.getPatient().getLastName(),
			invitation.getStatus(),
			invitation.getStartDate(),
			invitation.getEndDate(),
			invitation.getExpiresAt(),
			userAlreadyRegistered,
			professionalProfileAlreadyCreated
		);
	}

	public TherapeuticTeamResponseDto acceptInvitation(String token) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PROFESSIONAL");

		TherapeuticTeamInvitation invitation = getInvitationByToken(token);
		refreshExpiredStatus(invitation);
		validatePendingInvitation(invitation);

		if (!currentUser.getEmail().equalsIgnoreCase(invitation.getInvitedEmail())) {
			throw new IllegalArgumentException("El usuario autenticado no coincide con el email invitado");
		}

		ProfessionalProfile professionalProfile = professionalProfileRepository.findByUserId(currentUser.getId())
			.orElseThrow(() -> new IllegalArgumentException("Debes crear tu perfil profesional antes de aceptar la invitacion"));

		boolean alreadyAssigned = therapeuticTeamService.isProfessionalAssignedToPatient(
			invitation.getPatient().getId(),
			professionalProfile.getId(),
			invitation.getStartDate()
		);

		if (alreadyAssigned) {
			throw new IllegalArgumentException("El profesional ya integra el equipo terapeutico del paciente para esa fecha");
		}

		TherapeuticTeamResponseDto createdTeam = therapeuticTeamService.createTherapeuticTeamFromInvitation(
			invitation.getPatient(),
			professionalProfile,
			invitation.getStartDate(),
			invitation.getEndDate()
		);

		invitation.setStatus(TherapeuticTeamInvitationStatus.ACCEPTED);
		invitation.setAcceptedAt(LocalDateTime.now());
		invitationRepository.save(invitation);

		return createdTeam;
	}

	public void cancelInvitation(UUID invitationId) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PARENT");

		TherapeuticTeamInvitation invitation = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new IllegalArgumentException("Invitacion no encontrada con ID: " + invitationId));

		getPatientOwnedByCurrentParent(invitation.getPatient().getId(), currentUser);
		validatePendingInvitation(invitation);

		invitation.setStatus(TherapeuticTeamInvitationStatus.CANCELLED);
		invitationRepository.save(invitation);
	}

	private Patient getPatientOwnedByCurrentParent(UUID patientId, User currentUser) {
		Patient patient = patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + patientId));

		if (patient.getParent() == null || !patient.getParent().getId().equals(currentUser.getId())) {
			throw new IllegalArgumentException("El tutor autenticado no tiene permisos sobre este paciente");
		}

		return patient;
	}

	private TherapeuticTeamInvitation getInvitationByToken(String token) {
		return invitationRepository.findByToken(token)
			.orElseThrow(() -> new IllegalArgumentException("Invitacion no encontrada para el token indicado"));
	}

	private void refreshExpiredStatus(TherapeuticTeamInvitation invitation) {
		if (invitation.getStatus() == TherapeuticTeamInvitationStatus.PENDING
			&& invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
			invitation.setStatus(TherapeuticTeamInvitationStatus.EXPIRED);
			invitationRepository.save(invitation);
		}
	}

	private void validatePendingInvitation(TherapeuticTeamInvitation invitation) {
		if (invitation.getStatus() != TherapeuticTeamInvitationStatus.PENDING) {
			throw new IllegalArgumentException("La invitacion ya no se encuentra pendiente");
		}

		if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
			invitation.setStatus(TherapeuticTeamInvitationStatus.EXPIRED);
			invitationRepository.save(invitation);
			throw new IllegalArgumentException("La invitacion ha expirado");
		}
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (endDate != null && endDate.isBefore(startDate)) {
			throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
		}
	}

	private void validateNoPendingInvitationOverlap(UUID patientId, String invitedEmail, LocalDate startDate, LocalDate endDate) {
		boolean overlaps = invitationRepository.findByPatientIdAndInvitedEmailIgnoreCaseAndStatus(
				patientId,
				invitedEmail.trim(),
				TherapeuticTeamInvitationStatus.PENDING
			)
			.stream()
			.peek(this::refreshExpiredStatus)
			.filter(invitation -> invitation.getStatus() == TherapeuticTeamInvitationStatus.PENDING)
			.anyMatch(invitation -> dateRangesOverlap(startDate, endDate, invitation.getStartDate(), invitation.getEndDate()));

		if (overlaps) {
			throw new IllegalArgumentException("Ya existe una invitacion pendiente para ese profesional en ese rango de fechas");
		}
	}

	private boolean dateRangesOverlap(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
		LocalDate effectiveEndA = endA != null ? endA : LocalDate.MAX;
		LocalDate effectiveEndB = endB != null ? endB : LocalDate.MAX;
		return !startA.isAfter(effectiveEndB) && !startB.isAfter(effectiveEndA);
	}

	private TherapeuticTeamInvitationResponseDto mapToResponseDto(TherapeuticTeamInvitation invitation) {
		return new TherapeuticTeamInvitationResponseDto(
			invitation.getId(),
			mapPatientToResponseDto(invitation.getPatient()),
			invitation.getInvitedEmail(),
			mapUserToBasicDto(invitation.getInvitedByUser()),
			invitation.getStatus(),
			invitation.getStartDate(),
			invitation.getEndDate(),
			invitation.getExpiresAt(),
			invitation.getAcceptedAt(),
			invitation.getCreatedAt(),
			invitation.getUpdatedAt()
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

	private UserBasicDto mapUserToBasicDto(User user) {
		return new UserBasicDto(
			user.getId(),
			user.getEmail(),
			user.getFirstName(),
			user.getLastName()
		);
	}
}
