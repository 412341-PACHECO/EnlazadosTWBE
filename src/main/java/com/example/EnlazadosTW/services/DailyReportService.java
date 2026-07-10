package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.DailyReportCreateDto;
import com.example.EnlazadosTW.dtos.DailyReportResponseDto;
import com.example.EnlazadosTW.dtos.DailyReportUpdateDto;
import com.example.EnlazadosTW.dtos.InstitutionBasicDto;
import com.example.EnlazadosTW.dtos.PatientResponseDto;
import com.example.EnlazadosTW.dtos.UserBasicDto;
import com.example.EnlazadosTW.entities.DailyReport;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.entities.TherapeuticTeam;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.enums.DailyReportPriority;
import com.example.EnlazadosTW.repositories.DailyReportRepository;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.ProfessionalProfileRepository;
import com.example.EnlazadosTW.repositories.TherapeuticTeamRepository;
import com.example.EnlazadosTW.repositories.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar reportes diarios del legajo interdisciplinario.
 */
@Service
@Transactional
public class DailyReportService {

	private final DailyReportRepository dailyReportRepository;
	private final PatientRepository patientRepository;
	private final UserRepository userRepository;
	private final ProfessionalProfileRepository professionalProfileRepository;
	private final TherapeuticTeamRepository therapeuticTeamRepository;
	private final TherapeuticTeamService therapeuticTeamService;
	private final FcmNotificationService fcmNotificationService;

	public DailyReportService(
		DailyReportRepository dailyReportRepository,
		PatientRepository patientRepository,
		UserRepository userRepository,
		ProfessionalProfileRepository professionalProfileRepository,
		TherapeuticTeamRepository therapeuticTeamRepository,
		TherapeuticTeamService therapeuticTeamService,
		FcmNotificationService fcmNotificationService
	) {
		this.dailyReportRepository = dailyReportRepository;
		this.patientRepository = patientRepository;
		this.userRepository = userRepository;
		this.professionalProfileRepository = professionalProfileRepository;
		this.therapeuticTeamRepository = therapeuticTeamRepository;
		this.therapeuticTeamService = therapeuticTeamService;
		this.fcmNotificationService = fcmNotificationService;
	}

	public DailyReportResponseDto createDailyReport(DailyReportCreateDto createDto) {
		Patient patient = getPatientById(createDto.patientId());
		User author = getUserById(createDto.authorId());

		validateAuthorBelongsToPatientTeam(patient.getId(), author.getId());

		DailyReport dailyReport = DailyReport.builder()
			.patient(patient)
			.author(author)
			.content(createDto.content())
			.priority(createDto.priority())
			.sentimentScore(createDto.sentimentScore())
			.build();

		DailyReport savedDailyReport = dailyReportRepository.save(dailyReport);
		dispatchCriticalPushNotificationIfNeeded(savedDailyReport);

		return mapToResponseDto(savedDailyReport);
	}

	@Transactional(readOnly = true)
	public DailyReportResponseDto getDailyReportById(UUID id) {
		return mapToResponseDto(getDailyReportEntityById(id));
	}

	@Transactional(readOnly = true)
	public List<DailyReportResponseDto> getAllDailyReports() {
		return dailyReportRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<DailyReportResponseDto> getDailyReportsByPatientId(UUID patientId) {
		return dailyReportRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<DailyReportResponseDto> getDailyReportsByAuthorId(UUID authorId) {
		return dailyReportRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<DailyReportResponseDto> getDailyReportsByPatientIdAndAuthorId(UUID patientId, UUID authorId) {
		return dailyReportRepository.findByPatientIdAndAuthorIdOrderByCreatedAtDesc(patientId, authorId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	public DailyReportResponseDto updateDailyReport(UUID id, DailyReportUpdateDto updateDto) {
		DailyReport dailyReport = getDailyReportEntityById(id);

		Patient patient = dailyReport.getPatient();
		if (updateDto.patientId() != null) {
			patient = getPatientById(updateDto.patientId());
			dailyReport.setPatient(patient);
		}

		User author = dailyReport.getAuthor();
		if (updateDto.authorId() != null) {
			author = getUserById(updateDto.authorId());
			dailyReport.setAuthor(author);
		}

		if (updateDto.patientId() != null || updateDto.authorId() != null) {
			validateAuthorBelongsToPatientTeam(patient.getId(), author.getId());
		}

		if (updateDto.content() != null) {
			dailyReport.setContent(updateDto.content());
		}

		if (updateDto.priority() != null) {
			dailyReport.setPriority(updateDto.priority());
		}

		if (updateDto.sentimentScore() != null) {
			dailyReport.setSentimentScore(updateDto.sentimentScore());
		}

		return mapToResponseDto(dailyReportRepository.save(dailyReport));
	}

	public void deleteDailyReport(UUID id) {
		dailyReportRepository.delete(getDailyReportEntityById(id));
	}

	private DailyReport getDailyReportEntityById(UUID id) {
		return dailyReportRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Reporte diario no encontrado con ID: " + id));
	}

	private Patient getPatientById(UUID patientId) {
		return patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + patientId));
	}

	private User getUserById(UUID authorId) {
		return userRepository.findById(authorId)
			.orElseThrow(() -> new IllegalArgumentException("Usuario autor no encontrado con ID: " + authorId));
	}

	private void validateAuthorBelongsToPatientTeam(UUID patientId, UUID authorId) {
		ProfessionalProfile professionalProfile = professionalProfileRepository.findByUserId(authorId)
			.orElseThrow(() -> new IllegalArgumentException("El autor no posee un perfil profesional"));

		boolean assigned = therapeuticTeamService.isProfessionalAssignedToPatient(
			patientId,
			professionalProfile.getId(),
			LocalDate.now()
		);

		if (!assigned) {
			throw new IllegalArgumentException("El profesional no integra el equipo terapeutico activo del paciente");
		}
	}

	private void dispatchCriticalPushNotificationIfNeeded(DailyReport dailyReport) {
		if (!shouldNotify(dailyReport.getPriority())) {
			return;
		}

		List<String> recipientTokens = resolveNotificationTokens(dailyReport);
		if (recipientTokens.isEmpty()) {
			return;
		}

		String normalizedPriority = translatePriority(dailyReport.getPriority());
		String patientFullName = buildPatientFullName(dailyReport.getPatient());
		String title = "Alerta " + normalizedPriority;
		String body = "Nuevo reporte de " + patientFullName + " con prioridad " + normalizedPriority + ".";

		Map<String, String> data = Map.of(
			"patientId", dailyReport.getPatient().getId().toString(),
			"dailyReportId", dailyReport.getId().toString(),
			"priority", dailyReport.getPriority().name()
		);

		fcmNotificationService.sendToTokens(recipientTokens, title, body, data);
	}

	private boolean shouldNotify(DailyReportPriority priority) {
		return DailyReportPriority.HIGH.equals(priority) || DailyReportPriority.CRITICAL.equals(priority);
	}

	private List<String> resolveNotificationTokens(DailyReport dailyReport) {
		List<User> recipients = new ArrayList<>();

		Patient patient = dailyReport.getPatient();
		if (patient.getParent() != null) {
			recipients.add(patient.getParent());
		}

		LocalDate today = LocalDate.now();
		List<User> teamUsers = therapeuticTeamRepository.findByPatientId(patient.getId())
			.stream()
			.filter(team -> isActiveTeam(team, today))
			.map(TherapeuticTeam::getProfessional)
			.map(ProfessionalProfile::getUser)
			.toList();

		recipients.addAll(teamUsers);

		return recipients.stream()
			.filter(user -> !user.getId().equals(dailyReport.getAuthor().getId()))
			.map(User::getFcmToken)
			.filter(token -> token != null && !token.isBlank())
			.map(String::trim)
			.distinct()
			.collect(Collectors.toList());
	}

	private boolean isActiveTeam(TherapeuticTeam therapeuticTeam, LocalDate targetDate) {
		boolean startsBeforeOrOnDate = !therapeuticTeam.getStartDate().isAfter(targetDate);
		boolean endsAfterOrOnDate = therapeuticTeam.getEndDate() == null || !therapeuticTeam.getEndDate().isBefore(targetDate);
		return startsBeforeOrOnDate && endsAfterOrOnDate;
	}

	private String buildPatientFullName(Patient patient) {
		String firstName = patient.getFirstName() != null ? patient.getFirstName().trim() : "";
		String lastName = patient.getLastName() != null ? patient.getLastName().trim() : "";
		String fullName = (firstName + " " + lastName).trim();
		return fullName.isBlank() ? "el paciente" : fullName;
	}

	private String translatePriority(DailyReportPriority priority) {
		return switch (priority) {
			case HIGH -> "ALTA";
			case CRITICAL -> "CRITICA";
			case MEDIUM -> "MEDIA";
			case LOW -> "BAJA";
		};
	}

	private DailyReportResponseDto mapToResponseDto(DailyReport dailyReport) {
		return new DailyReportResponseDto(
			dailyReport.getId(),
			mapPatientToResponseDto(dailyReport.getPatient()),
			mapUserToBasicDto(dailyReport.getAuthor()),
			dailyReport.getContent(),
			dailyReport.getPriority(),
			dailyReport.getSentimentScore(),
			dailyReport.getCreatedAt(),
			dailyReport.getUpdatedAt()
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
