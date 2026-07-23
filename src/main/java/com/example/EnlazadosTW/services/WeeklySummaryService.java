package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.PatientBasicDto;
import com.example.EnlazadosTW.dtos.WeeklySummaryGenerateDto;
import com.example.EnlazadosTW.dtos.WeeklySummaryResponseDto;
import com.example.EnlazadosTW.entities.DailyReport;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.TherapeuticTeam;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.entities.WeeklySummary;
import com.example.EnlazadosTW.repositories.DailyReportRepository;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.TherapeuticTeamRepository;
import com.example.EnlazadosTW.repositories.WeeklySummaryRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para generar y consultar resúmenes semanales automáticos.
 */
@Service
@Transactional
public class WeeklySummaryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(WeeklySummaryService.class);

	private final WeeklySummaryRepository weeklySummaryRepository;
	private final DailyReportRepository dailyReportRepository;
	private final PatientRepository patientRepository;
	private final TherapeuticTeamRepository therapeuticTeamRepository;
	private final FcmNotificationService fcmNotificationService;
	private final ChatClient chatClient;
	private final String configuredModelName;

	public WeeklySummaryService(
		WeeklySummaryRepository weeklySummaryRepository,
		DailyReportRepository dailyReportRepository,
		PatientRepository patientRepository,
		TherapeuticTeamRepository therapeuticTeamRepository,
		FcmNotificationService fcmNotificationService,
		ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
		@Value("${spring.ai.google.genai.chat.model:gemini-3.5-flash}") String configuredModelName
	) {
		this.weeklySummaryRepository = weeklySummaryRepository;
		this.dailyReportRepository = dailyReportRepository;
		this.patientRepository = patientRepository;
		this.therapeuticTeamRepository = therapeuticTeamRepository;
		this.fcmNotificationService = fcmNotificationService;
		ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
		this.chatClient = builder != null ? builder.build() : null;
		this.configuredModelName = configuredModelName;
	}

	public WeeklySummaryResponseDto generateWeeklySummary(WeeklySummaryGenerateDto generateDto) {
		Patient patient = getPatientById(generateDto.patientId());
		LocalDate referenceDate = generateDto.referenceDate() != null ? generateDto.referenceDate() : LocalDate.now();
		WeeklyWindow weeklyWindow = resolveWeeklyWindow(referenceDate);

		List<DailyReport> reports = dailyReportRepository.findByPatientIdAndCreatedAtBetweenOrderByCreatedAtAsc(
			patient.getId(),
			weeklyWindow.startDateTime(),
			weeklyWindow.endDateTime()
		);

		if (reports.isEmpty()) {
			throw new IllegalArgumentException(
				"No hay reportes diarios para el paciente en la semana " +
				weeklyWindow.weekStart() +
				" - " +
				weeklyWindow.weekEnd()
			);
		}

		WeeklySummary savedSummary = createOrUpdateSummary(patient, reports, weeklyWindow);
		return mapToResponseDto(savedSummary);
	}

	@Transactional(readOnly = true)
	public WeeklySummaryResponseDto getWeeklySummaryById(UUID id) {
		return mapToResponseDto(getWeeklySummaryEntityById(id));
	}

	@Transactional(readOnly = true)
	public List<WeeklySummaryResponseDto> getWeeklySummariesByPatientId(UUID patientId) {
		return weeklySummaryRepository.findByPatientIdOrderByWeekStartDesc(patientId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public WeeklySummaryResponseDto getLatestWeeklySummaryByPatientId(UUID patientId) {
		WeeklySummary summary = weeklySummaryRepository.findTopByPatientIdOrderByWeekStartDesc(patientId)
			.orElseThrow(() -> new IllegalArgumentException("No existen resúmenes semanales para el paciente"));
		return mapToResponseDto(summary);
	}

	public void generateCurrentWeekSummaries() {
		WeeklyWindow weeklyWindow = resolveWeeklyWindow(LocalDate.now());
		List<DailyReport> reports = dailyReportRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(
			weeklyWindow.startDateTime(),
			weeklyWindow.endDateTime()
		);

		if (reports.isEmpty()) {
			LOGGER.info(
				"No se generaron resúmenes semanales porque no hay reportes entre {} y {}",
				weeklyWindow.weekStart(),
				weeklyWindow.weekEnd()
			);
			return;
		}

		Map<UUID, List<DailyReport>> reportsByPatient = reports.stream()
			.collect(Collectors.groupingBy(report -> report.getPatient().getId(), LinkedHashMap::new, Collectors.toList()));

		reportsByPatient.values().forEach(patientReports -> {
			try {
				createOrUpdateSummary(patientReports.getFirst().getPatient(), patientReports, weeklyWindow);
			}
			catch (Exception exception) {
				LOGGER.error(
					"Error al generar el resumen semanal para el paciente {}",
					patientReports.getFirst().getPatient().getId(),
					exception
				);
			}
		});
	}

	private WeeklySummary createOrUpdateSummary(Patient patient, List<DailyReport> reports, WeeklyWindow weeklyWindow) {
		WeeklySummary weeklySummary = weeklySummaryRepository.findByPatientIdAndWeekStartAndWeekEnd(
			patient.getId(),
			weeklyWindow.weekStart(),
			weeklyWindow.weekEnd()
		).orElseGet(WeeklySummary::new);

		GeneratedSummary generatedSummary = generateSummaryContent(patient, reports, weeklyWindow);

		weeklySummary.setPatient(patient);
		weeklySummary.setWeekStart(weeklyWindow.weekStart());
		weeklySummary.setWeekEnd(weeklyWindow.weekEnd());
		weeklySummary.setSummaryContent(generatedSummary.summaryContent());
		weeklySummary.setReportsCount(reports.size());
		weeklySummary.setGeneratedAt(LocalDateTime.now());
		weeklySummary.setModelName(generatedSummary.modelName());

		WeeklySummary savedSummary = weeklySummaryRepository.save(weeklySummary);
		dispatchWeeklySummaryNotification(savedSummary);
		return savedSummary;
	}

	private GeneratedSummary generateSummaryContent(Patient patient, List<DailyReport> reports, WeeklyWindow weeklyWindow) {
		if (chatClient == null) {
			return new GeneratedSummary(buildFallbackSummary(patient, reports, weeklyWindow), "LOCAL_FALLBACK");
		}

		try {
			String content = chatClient.prompt()
				.system(buildSystemPrompt())
				.user(buildUserPrompt(patient, reports, weeklyWindow))
				.call()
				.content();

			if (content == null || content.isBlank()) {
				return new GeneratedSummary(buildFallbackSummary(patient, reports, weeklyWindow), "LOCAL_FALLBACK");
			}

			return new GeneratedSummary(content.trim(), configuredModelName);
		}
		catch (Exception exception) {
			LOGGER.warn("No se pudo generar el resumen semanal con Gemini. Se utiliza fallback local.", exception);
			return new GeneratedSummary(buildFallbackSummary(patient, reports, weeklyWindow), "LOCAL_FALLBACK");
		}
	}

	private String buildSystemPrompt() {
		return """
			Sos un asistente clinico administrativo de EnlazadosTW.
			Tenés que resumir reportes diarios interdisciplinarios en español claro, profesional y conciso.
			No inventes datos.
			No hagas diagnosticos nuevos.
			No des indicaciones medicas.
			Resumí hechos observables, evolución, alertas relevantes y continuidad sugerida del seguimiento.
			Estructurá la salida en 3 bloques:
			1. Evolucion general
			2. Hallazgos relevantes
			3. Alertas o seguimiento recomendado
			""";
	}

	private String buildUserPrompt(Patient patient, List<DailyReport> reports, WeeklyWindow weeklyWindow) {
		String patientFullName = buildPatientFullName(patient);
		String reportsContent = reports.stream()
			.map(report -> {
				String authorName = buildAuthorFullName(report);
				return """
					- Fecha: %s
					  Autor: %s
					  Prioridad: %s
					  Contenido: %s
					""".formatted(
					report.getCreatedAt(),
					authorName,
					report.getPriority().name(),
					report.getContent()
				);
			})
			.collect(Collectors.joining(System.lineSeparator()));

		return """
			Generá un resumen semanal del legajo interdisciplinario del paciente.

			Paciente: %s
			Periodo: %s a %s
			Cantidad de reportes: %d

			Reportes:
			%s
			""".formatted(
			patientFullName,
			weeklyWindow.weekStart(),
			weeklyWindow.weekEnd(),
			reports.size(),
			reportsContent
		);
	}

	private String buildFallbackSummary(Patient patient, List<DailyReport> reports, WeeklyWindow weeklyWindow) {
		Map<String, Long> priorities = reports.stream()
			.collect(Collectors.groupingBy(report -> report.getPriority().name(), LinkedHashMap::new, Collectors.counting()));

		String prioritiesLine = priorities.entrySet().stream()
			.map(entry -> entry.getKey() + ": " + entry.getValue())
			.collect(Collectors.joining(", "));

		String recentHighlights = reports.stream()
			.limit(3)
			.map(report -> {
				String content = report.getContent().trim();
				String truncated = content.length() > 180 ? content.substring(0, 180) + "..." : content;
				return "- " + truncated;
			})
			.collect(Collectors.joining(System.lineSeparator()));

		return """
			1. Evolucion general
			Se registraron %d reportes del paciente %s entre %s y %s.

			2. Hallazgos relevantes
			Distribucion de prioridades: %s
			Observaciones destacadas:
			%s

			3. Alertas o seguimiento recomendado
			Continuar el seguimiento interdisciplinario y revisar los reportes detallados para la toma de decisiones del equipo.
			""".formatted(
			reports.size(),
			buildPatientFullName(patient),
			weeklyWindow.weekStart(),
			weeklyWindow.weekEnd(),
			prioritiesLine.isBlank() ? "sin datos" : prioritiesLine,
			recentHighlights.isBlank() ? "- Sin observaciones adicionales." : recentHighlights
		);
	}

	private WeeklyWindow resolveWeeklyWindow(LocalDate referenceDate) {
		LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate weekEnd = weekStart.plusDays(4);
		LocalDateTime startDateTime = weekStart.atStartOfDay();
		LocalDateTime endDateTime = weekEnd.plusDays(1).atStartOfDay().minusNanos(1);
		return new WeeklyWindow(weekStart, weekEnd, startDateTime, endDateTime);
	}

	private WeeklySummary getWeeklySummaryEntityById(UUID id) {
		return weeklySummaryRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Resumen semanal no encontrado con ID: " + id));
	}

	private Patient getPatientById(UUID patientId) {
		return patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + patientId));
	}

	private WeeklySummaryResponseDto mapToResponseDto(WeeklySummary weeklySummary) {
		return new WeeklySummaryResponseDto(
			weeklySummary.getId(),
			new PatientBasicDto(
				weeklySummary.getPatient().getId(),
				weeklySummary.getPatient().getFirstName(),
				weeklySummary.getPatient().getLastName(),
				weeklySummary.getPatient().getDiagnosis()
			),
			weeklySummary.getWeekStart(),
			weeklySummary.getWeekEnd(),
			weeklySummary.getSummaryContent(),
			weeklySummary.getReportsCount(),
			weeklySummary.getGeneratedAt(),
			weeklySummary.getModelName(),
			weeklySummary.getCreatedAt(),
			weeklySummary.getUpdatedAt()
		);
	}

	private String buildPatientFullName(Patient patient) {
		String firstName = patient.getFirstName() != null ? patient.getFirstName().trim() : "";
		String lastName = patient.getLastName() != null ? patient.getLastName().trim() : "";
		String fullName = (firstName + " " + lastName).trim();
		return fullName.isBlank() ? "Paciente sin nombre" : fullName;
	}

	private String buildAuthorFullName(DailyReport report) {
		String firstName = report.getAuthor().getFirstName() != null ? report.getAuthor().getFirstName().trim() : "";
		String lastName = report.getAuthor().getLastName() != null ? report.getAuthor().getLastName().trim() : "";
		String fullName = (firstName + " " + lastName).trim();
		return fullName.isBlank() ? report.getAuthor().getEmail() : fullName;
	}

	private void dispatchWeeklySummaryNotification(WeeklySummary weeklySummary) {
		List<String> recipientTokens = resolveNotificationTokens(weeklySummary.getPatient(), weeklySummary.getWeekEnd());
		if (recipientTokens.isEmpty()) {
			return;
		}

		String patientFullName = buildPatientFullName(weeklySummary.getPatient());
		String title = "Resumen semanal disponible";
		String body = "Ya esta disponible el resumen semanal de " + patientFullName + ".";

		Map<String, String> data = Map.of(
			"type", "weekly_summary",
			"weeklySummaryId", weeklySummary.getId().toString(),
			"patientId", weeklySummary.getPatient().getId().toString(),
			"weekStart", weeklySummary.getWeekStart().toString(),
			"weekEnd", weeklySummary.getWeekEnd().toString()
		);

		fcmNotificationService.sendToTokens(recipientTokens, title, body, data);
	}

	private List<String> resolveNotificationTokens(Patient patient, LocalDate targetDate) {
		List<User> recipients = new ArrayList<>();

		if (patient.getParent() != null) {
			recipients.add(patient.getParent());
		}

		List<User> teamUsers = therapeuticTeamRepository.findByPatientId(patient.getId())
			.stream()
			.filter(team -> isActiveTeam(team, targetDate))
			.map(TherapeuticTeam::getProfessional)
			.map(professional -> professional.getUser())
			.toList();

		recipients.addAll(teamUsers);

		return recipients.stream()
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

	private record WeeklyWindow(
		LocalDate weekStart,
		LocalDate weekEnd,
		LocalDateTime startDateTime,
		LocalDateTime endDateTime
	) {}

	private record GeneratedSummary(String summaryContent, String modelName) {}
}
