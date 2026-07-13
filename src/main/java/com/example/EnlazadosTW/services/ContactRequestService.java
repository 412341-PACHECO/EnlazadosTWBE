package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.ContactRequestCreateDto;
import com.example.EnlazadosTW.dtos.ContactRequestResponseDto;
import com.example.EnlazadosTW.dtos.ParentContactRequestResponseDto;
import com.example.EnlazadosTW.dtos.ProfessionalContactSummaryDto;
import com.example.EnlazadosTW.entities.ContactRequest;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.enums.ContactRequestStatus;
import com.example.EnlazadosTW.repositories.ContactRequestRepository;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.ProfessionalProfileRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar solicitudes de contacto entre tutores y profesionales.
 */
@Service
@Transactional
public class ContactRequestService {

	private final ContactRequestRepository contactRequestRepository;
	private final ProfessionalProfileRepository professionalProfileRepository;
	private final PatientRepository patientRepository;
	private final CurrentUserService currentUserService;
	private final FcmNotificationService fcmNotificationService;

	public ContactRequestService(
		ContactRequestRepository contactRequestRepository,
		ProfessionalProfileRepository professionalProfileRepository,
		PatientRepository patientRepository,
		CurrentUserService currentUserService,
		FcmNotificationService fcmNotificationService
	) {
		this.contactRequestRepository = contactRequestRepository;
		this.professionalProfileRepository = professionalProfileRepository;
		this.patientRepository = patientRepository;
		this.currentUserService = currentUserService;
		this.fcmNotificationService = fcmNotificationService;
	}

	public ContactRequestResponseDto createContactRequest(ContactRequestCreateDto createDto) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PARENT");

		ProfessionalProfile professionalProfile = getProfessionalProfileById(createDto.professionalId());
		Patient patient = getOwnedPatientIfPresent(createDto.patientId(), currentUser);

		String parentPhone = resolveParentPhone(createDto);
		String parentEmail = Boolean.TRUE.equals(createDto.shareEmail()) ? currentUser.getEmail() : null;
		String parentFullName = buildFullName(currentUser.getFirstName(), currentUser.getLastName());

		ContactRequest contactRequest = ContactRequest.builder()
			.parentUser(currentUser)
			.professionalProfile(professionalProfile)
			.patient(patient)
			.parentFullName(parentFullName)
			.parentEmail(parentEmail)
			.parentPhone(parentPhone)
			.message(normalizeText(createDto.message()))
			.status(ContactRequestStatus.PENDING)
			.build();

		ContactRequest savedRequest = contactRequestRepository.save(contactRequest);
		sendPushNotificationToProfessional(savedRequest);

		return mapToResponseDto(savedRequest);
	}

	@Transactional(readOnly = true)
	public List<ContactRequestResponseDto> getContactRequestsByProfessional(UUID professionalId) {
		ProfessionalProfile professionalProfile = getAuthenticatedProfessionalProfile(professionalId);

		return contactRequestRepository.findByProfessionalProfileIdOrderByCreatedAtDesc(professionalProfile.getId())
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<ParentContactRequestResponseDto> getContactRequestsByParent(UUID parentId) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PARENT");

		if (!currentUser.getId().equals(parentId)) {
			throw new IllegalArgumentException("El tutor autenticado no tiene permisos sobre estas solicitudes");
		}

		return contactRequestRepository.findByParentUserIdOrderByCreatedAtDesc(parentId)
			.stream()
			.map(this::mapToParentResponseDto)
			.toList();
	}

	public ContactRequestResponseDto markContactRequestAsViewed(UUID requestId) {
		ContactRequest contactRequest = getOwnedContactRequestForAuthenticatedProfessional(requestId);
		contactRequest.setStatus(ContactRequestStatus.VIEWED);
		return mapToResponseDto(contactRequestRepository.save(contactRequest));
	}

	public int markAllContactRequestsAsViewed(UUID professionalId) {
		ProfessionalProfile professionalProfile = getAuthenticatedProfessionalProfile(professionalId);

		List<ContactRequest> pendingRequests = contactRequestRepository.findByProfessionalProfileIdAndStatus(
			professionalProfile.getId(),
			ContactRequestStatus.PENDING
		);

		pendingRequests.forEach(request -> request.setStatus(ContactRequestStatus.VIEWED));
		contactRequestRepository.saveAll(pendingRequests);
		return pendingRequests.size();
	}

	private ProfessionalProfile getProfessionalProfileById(UUID professionalId) {
		return professionalProfileRepository.findById(professionalId)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + professionalId));
	}

	private ProfessionalProfile getAuthenticatedProfessionalProfile(UUID professionalId) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PROFESSIONAL");

		ProfessionalProfile professionalProfile = professionalProfileRepository.findByUserId(currentUser.getId())
			.orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no tiene perfil profesional"));

		if (!professionalProfile.getId().equals(professionalId)) {
			throw new IllegalArgumentException("El profesional autenticado no tiene permisos sobre estas solicitudes");
		}

		return professionalProfile;
	}

	private ContactRequest getOwnedContactRequestForAuthenticatedProfessional(UUID requestId) {
		User currentUser = currentUserService.getCurrentAuthenticatedUser();
		currentUserService.requireRole(currentUser, "PROFESSIONAL");

		ContactRequest contactRequest = contactRequestRepository.findById(requestId)
			.orElseThrow(() -> new IllegalArgumentException("Solicitud de contacto no encontrada con ID: " + requestId));

		ProfessionalProfile professionalProfile = professionalProfileRepository.findByUserId(currentUser.getId())
			.orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no tiene perfil profesional"));

		if (!contactRequest.getProfessionalProfile().getId().equals(professionalProfile.getId())) {
			throw new IllegalArgumentException("El profesional autenticado no tiene permisos sobre esta solicitud");
		}

		return contactRequest;
	}

	private Patient getOwnedPatientIfPresent(UUID patientId, User currentUser) {
		if (patientId == null) {
			return null;
		}

		Patient patient = patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + patientId));

		if (patient.getParent() == null || !patient.getParent().getId().equals(currentUser.getId())) {
			throw new IllegalArgumentException("El paciente no pertenece al tutor autenticado");
		}

		return patient;
	}

	private String resolveParentPhone(ContactRequestCreateDto createDto) {
		if (!Boolean.TRUE.equals(createDto.sharePhone())) {
			return null;
		}

		String parentPhone = normalizeText(createDto.parentPhone());
		if (parentPhone == null) {
			throw new IllegalArgumentException("Si se comparte el telefono, el numero es obligatorio");
		}

		return parentPhone;
	}

	private void sendPushNotificationToProfessional(ContactRequest contactRequest) {
		User professionalUser = contactRequest.getProfessionalProfile().getUser();
		String token = professionalUser.getFcmToken();
		if (token == null || token.isBlank()) {
			return;
		}

		String patientFullName = contactRequest.getPatient() != null
			? buildFullName(contactRequest.getPatient().getFirstName(), contactRequest.getPatient().getLastName())
			: null;

		Map<String, String> data = Map.of(
			"type", "contact_request",
			"requestId", contactRequest.getId().toString(),
			"parentFullName", contactRequest.getParentFullName(),
			"parentEmail", valueOrEmpty(contactRequest.getParentEmail()),
			"parentPhone", valueOrEmpty(contactRequest.getParentPhone()),
			"patientFullName", valueOrEmpty(patientFullName),
			"message", valueOrEmpty(contactRequest.getMessage())
		);

		fcmNotificationService.sendToToken(
			token.trim(),
			"Nueva solicitud de contacto",
			"Una familia quiere comunicarse con vos desde EnlazadosTW",
			data
		);
	}

	private ContactRequestResponseDto mapToResponseDto(ContactRequest contactRequest) {
		String patientFullName = null;
		UUID patientId = null;
		if (contactRequest.getPatient() != null) {
			patientId = contactRequest.getPatient().getId();
			patientFullName = buildFullName(
				contactRequest.getPatient().getFirstName(),
				contactRequest.getPatient().getLastName()
			);
		}

		return new ContactRequestResponseDto(
			contactRequest.getId(),
			contactRequest.getParentUser().getId(),
			contactRequest.getProfessionalProfile().getId(),
			patientId,
			contactRequest.getParentFullName(),
			contactRequest.getParentEmail(),
			contactRequest.getParentPhone(),
			patientFullName,
			contactRequest.getMessage(),
			contactRequest.getStatus(),
			contactRequest.getCreatedAt(),
			contactRequest.getUpdatedAt()
		);
	}

	private ParentContactRequestResponseDto mapToParentResponseDto(ContactRequest contactRequest) {
		String patientFullName = null;
		UUID patientId = null;
		if (contactRequest.getPatient() != null) {
			patientId = contactRequest.getPatient().getId();
			patientFullName = buildFullName(
				contactRequest.getPatient().getFirstName(),
				contactRequest.getPatient().getLastName()
			);
		}

		ProfessionalProfile professionalProfile = contactRequest.getProfessionalProfile();
		ProfessionalContactSummaryDto professionalDto = new ProfessionalContactSummaryDto(
			professionalProfile.getId(),
			professionalProfile.getUser().getId(),
			professionalProfile.getUser().getFirstName(),
			professionalProfile.getUser().getLastName(),
			professionalProfile.getSpecialty(),
			professionalProfile.getAcceptedHealthInsurances(),
			professionalProfile.getSessionFee()
		);

		return new ParentContactRequestResponseDto(
			contactRequest.getId(),
			contactRequest.getParentUser().getId(),
			professionalProfile.getId(),
			patientId,
			contactRequest.getParentFullName(),
			contactRequest.getParentEmail(),
			contactRequest.getParentPhone(),
			patientFullName,
			contactRequest.getMessage(),
			contactRequest.getStatus(),
			professionalDto,
			contactRequest.getCreatedAt(),
			contactRequest.getUpdatedAt()
		);
	}

	private String buildFullName(String firstName, String lastName) {
		String safeFirstName = firstName != null ? firstName.trim() : "";
		String safeLastName = lastName != null ? lastName.trim() : "";
		String fullName = (safeFirstName + " " + safeLastName).trim();
		return fullName.isBlank() ? null : fullName;
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}

		String trimmedValue = value.trim();
		return trimmedValue.isBlank() ? null : trimmedValue;
	}

	private String valueOrEmpty(String value) {
		return value != null ? value : "";
	}
}
