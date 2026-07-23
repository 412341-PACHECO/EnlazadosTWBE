package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.AttendanceBillingGenerateDto;
import com.example.EnlazadosTW.dtos.AttendanceBillingResponseDto;
import com.example.EnlazadosTW.dtos.AttendanceBillingStatusUpdateDto;
import com.example.EnlazadosTW.dtos.PatientBasicDto;
import com.example.EnlazadosTW.dtos.ProfessionalContactSummaryDto;
import com.example.EnlazadosTW.entities.AttendanceBilling;
import com.example.EnlazadosTW.entities.AttendanceRecord;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.enums.AttendanceBillingStatus;
import com.example.EnlazadosTW.enums.AttendanceRecordStatus;
import com.example.EnlazadosTW.repositories.AttendanceBillingRepository;
import com.example.EnlazadosTW.repositories.AttendanceRecordRepository;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.ProfessionalProfileRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para consolidar asistencias en liquidaciones mensuales.
 */
@Service
@Transactional
public class AttendanceBillingService {

	private final AttendanceBillingRepository attendanceBillingRepository;
	private final AttendanceRecordRepository attendanceRecordRepository;
	private final ProfessionalProfileRepository professionalProfileRepository;
	private final PatientRepository patientRepository;

	public AttendanceBillingService(
		AttendanceBillingRepository attendanceBillingRepository,
		AttendanceRecordRepository attendanceRecordRepository,
		ProfessionalProfileRepository professionalProfileRepository,
		PatientRepository patientRepository
	) {
		this.attendanceBillingRepository = attendanceBillingRepository;
		this.attendanceRecordRepository = attendanceRecordRepository;
		this.professionalProfileRepository = professionalProfileRepository;
		this.patientRepository = patientRepository;
	}

	public AttendanceBillingResponseDto generateAttendanceBilling(AttendanceBillingGenerateDto generateDto) {
		ProfessionalProfile professionalProfile = getProfessionalProfileById(generateDto.professionalId());
		Patient patient = getPatientIfPresent(generateDto.patientId());
		YearMonth yearMonth = parseBillingPeriod(generateDto.billingPeriod());
		String normalizedHealthInsuranceName = normalizeText(generateDto.healthInsuranceName());

		List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByProfessionalProfileIdOrderBySessionDateDesc(
				generateDto.professionalId()
			)
			.stream()
			.filter(record -> AttendanceRecordStatus.PENDING.equals(record.getStatus()))
			.filter(record -> record.getAttendanceBilling() == null)
			.filter(record -> YearMonth.from(record.getSessionDate()).equals(yearMonth))
			.filter(record -> patient == null || record.getPatient().getId().equals(patient.getId()))
			.filter(record -> matchesHealthInsurance(record, normalizedHealthInsuranceName))
			.toList();

		if (attendanceRecords.isEmpty()) {
			throw new IllegalArgumentException("No existen asistencias pendientes para consolidar con los filtros indicados");
		}

		BigDecimal totalAmount = attendanceRecords.stream()
			.map(AttendanceRecord::getSessionFeeSnapshot)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		AttendanceBilling attendanceBilling = AttendanceBilling.builder()
			.professionalProfile(professionalProfile)
			.patient(patient)
			.billingPeriod(generateDto.billingPeriod())
			.healthInsuranceName(normalizedHealthInsuranceName)
			.totalSessions(attendanceRecords.size())
			.totalAmount(totalAmount)
			.paymentStatus(AttendanceBillingStatus.PENDING)
			.digitalHash(generateDigitalHash(
				professionalProfile,
				patient,
				generateDto.billingPeriod(),
				normalizedHealthInsuranceName,
				attendanceRecords,
				totalAmount
			))
			.build();

		AttendanceBilling savedBilling = attendanceBillingRepository.save(attendanceBilling);

		attendanceRecords.forEach(record -> {
			record.setStatus(AttendanceRecordStatus.BILLED);
			record.setAttendanceBilling(savedBilling);
		});
		attendanceRecordRepository.saveAll(attendanceRecords);

		return mapToResponseDto(savedBilling, attendanceRecords);
	}

	@Transactional(readOnly = true)
	public AttendanceBillingResponseDto getAttendanceBillingById(UUID id) {
		AttendanceBilling billing = getAttendanceBillingEntityById(id);
		List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByAttendanceBillingIdOrderBySessionDateAsc(id);
		return mapToResponseDto(billing, attendanceRecords);
	}

	@Transactional(readOnly = true)
	public List<AttendanceBillingResponseDto> getAllAttendanceBillings() {
		return attendanceBillingRepository.findAll()
			.stream()
			.map(billing -> mapToResponseDto(
				billing,
				attendanceRecordRepository.findByAttendanceBillingIdOrderBySessionDateAsc(billing.getId())
			))
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AttendanceBillingResponseDto> getAttendanceBillingsByProfessionalId(UUID professionalId) {
		return getAttendanceBillingsByProfessionalId(professionalId, null, null);
	}

	@Transactional(readOnly = true)
	public List<AttendanceBillingResponseDto> getAttendanceBillingsByProfessionalId(
		UUID professionalId,
		LocalDate dateFrom,
		LocalDate dateTo
	) {
		validateDateRange(dateFrom, dateTo);

		return attendanceBillingRepository.findByProfessionalProfileIdOrderByCreatedAtDesc(professionalId)
			.stream()
			.map(billing -> {
				List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByAttendanceBillingIdOrderBySessionDateAsc(
					billing.getId()
				);
				List<AttendanceRecord> filteredAttendanceRecords = filterAttendanceRecordsByDateRange(
					attendanceRecords,
					dateFrom,
					dateTo
				);
				return filteredAttendanceRecords.isEmpty() ? null : mapToResponseDto(billing, filteredAttendanceRecords);
			})
			.filter(responseDto -> responseDto != null)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AttendanceBillingResponseDto> getAttendanceBillingsByPeriod(String billingPeriod) {
		return getAttendanceBillingsByPeriod(billingPeriod, null, null);
	}

	@Transactional(readOnly = true)
	public List<AttendanceBillingResponseDto> getAttendanceBillingsByPeriod(
		String billingPeriod,
		LocalDate dateFrom,
		LocalDate dateTo
	) {
		parseBillingPeriod(billingPeriod);
		validateDateRange(dateFrom, dateTo);

		return attendanceBillingRepository.findByBillingPeriodOrderByCreatedAtDesc(billingPeriod)
			.stream()
			.map(billing -> {
				List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByAttendanceBillingIdOrderBySessionDateAsc(
					billing.getId()
				);
				List<AttendanceRecord> filteredAttendanceRecords = filterAttendanceRecordsByDateRange(
					attendanceRecords,
					dateFrom,
					dateTo
				);
				return filteredAttendanceRecords.isEmpty() ? null : mapToResponseDto(billing, filteredAttendanceRecords);
			})
			.filter(responseDto -> responseDto != null)
			.toList();
	}

	public AttendanceBillingResponseDto updateAttendanceBillingStatus(
		UUID billingId,
		AttendanceBillingStatusUpdateDto updateDto
	) {
		AttendanceBilling billing = getAttendanceBillingEntityById(billingId);
		billing.setPaymentStatus(updateDto.paymentStatus());
		AttendanceBilling updatedBilling = attendanceBillingRepository.save(billing);

		List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByAttendanceBillingIdOrderBySessionDateAsc(billingId);
		return mapToResponseDto(updatedBilling, attendanceRecords);
	}

	private AttendanceBilling getAttendanceBillingEntityById(UUID id) {
		return attendanceBillingRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Liquidacion de asistencias no encontrada con ID: " + id));
	}

	private ProfessionalProfile getProfessionalProfileById(UUID professionalId) {
		return professionalProfileRepository.findById(professionalId)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + professionalId));
	}

	private Patient getPatientIfPresent(UUID patientId) {
		if (patientId == null) {
			return null;
		}

		return patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + patientId));
	}

	private YearMonth parseBillingPeriod(String billingPeriod) {
		try {
			return YearMonth.parse(billingPeriod);
		} catch (Exception ex) {
			throw new IllegalArgumentException("El periodo de facturacion debe tener formato YYYY-MM");
		}
	}

	private boolean matchesHealthInsurance(AttendanceRecord record, String healthInsuranceName) {
		if (healthInsuranceName == null) {
			return true;
		}

		return record.getHealthInsuranceName() != null
			&& record.getHealthInsuranceName().trim().toLowerCase(Locale.ROOT)
				.equals(healthInsuranceName.toLowerCase(Locale.ROOT));
	}

	private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
			throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
		}
	}

	private List<AttendanceRecord> filterAttendanceRecordsByDateRange(
		List<AttendanceRecord> attendanceRecords,
		LocalDate dateFrom,
		LocalDate dateTo
	) {
		if (dateFrom == null && dateTo == null) {
			return attendanceRecords;
		}

		return attendanceRecords.stream()
			.filter(record -> dateFrom == null || !record.getSessionDate().isBefore(dateFrom))
			.filter(record -> dateTo == null || !record.getSessionDate().isAfter(dateTo))
			.toList();
	}

	private AttendanceBillingResponseDto mapToResponseDto(
		AttendanceBilling billing,
		List<AttendanceRecord> attendanceRecords
	) {
		ProfessionalProfile professionalProfile = billing.getProfessionalProfile();
		ProfessionalContactSummaryDto professionalDto = new ProfessionalContactSummaryDto(
			professionalProfile.getId(),
			professionalProfile.getUser().getId(),
			professionalProfile.getUser().getFirstName(),
			professionalProfile.getUser().getLastName(),
			professionalProfile.getSpecialty(),
			professionalProfile.getAcceptedHealthInsurances(),
			professionalProfile.getSessionFee()
		);

		PatientBasicDto patientDto = null;
		if (billing.getPatient() != null) {
			patientDto = new PatientBasicDto(
				billing.getPatient().getId(),
				billing.getPatient().getFirstName(),
				billing.getPatient().getLastName(),
				billing.getPatient().getDiagnosis()
			);
		}

		List<UUID> attendanceRecordIds = attendanceRecords.stream()
			.map(AttendanceRecord::getId)
			.collect(Collectors.toList());

		BigDecimal totalAmount = attendanceRecords.stream()
			.map(AttendanceRecord::getSessionFeeSnapshot)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new AttendanceBillingResponseDto(
			billing.getId(),
			professionalDto,
			patientDto,
			billing.getBillingPeriod(),
			billing.getHealthInsuranceName(),
			attendanceRecords.size(),
			totalAmount,
			billing.getPaymentStatus(),
			billing.getDigitalHash(),
			attendanceRecordIds,
			billing.getCreatedAt(),
			billing.getUpdatedAt()
		);
	}

	private String generateDigitalHash(
		ProfessionalProfile professionalProfile,
		Patient patient,
		String billingPeriod,
		String healthInsuranceName,
		List<AttendanceRecord> attendanceRecords,
		BigDecimal totalAmount
	) {
		StringBuilder builder = new StringBuilder();
		builder.append(professionalProfile.getId())
			.append('|')
			.append(billingPeriod)
			.append('|')
			.append(healthInsuranceName != null ? healthInsuranceName : "")
			.append('|')
			.append(patient != null ? patient.getId() : "")
			.append('|')
			.append(totalAmount)
			.append('|')
			.append(attendanceRecords.size());

		for (AttendanceRecord attendanceRecord : attendanceRecords) {
			builder.append('|')
				.append(attendanceRecord.getId())
				.append('|')
				.append(attendanceRecord.getSessionDate())
				.append('|')
				.append(attendanceRecord.getPatient().getId())
				.append('|')
				.append(attendanceRecord.getSessionFeeSnapshot())
				.append('|')
				.append(attendanceRecord.getHealthInsuranceName());
		}

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = messageDigest.digest(builder.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("No se pudo generar el hash digital de la liquidacion", ex);
		}
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}

		String trimmedValue = value.trim();
		return trimmedValue.isBlank() ? null : trimmedValue;
	}
}
