package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.AttendanceRecordCreateDto;
import com.example.EnlazadosTW.dtos.AttendanceRecordResponseDto;
import com.example.EnlazadosTW.dtos.AttendanceRecordUpdateDto;
import com.example.EnlazadosTW.dtos.PatientBasicDto;
import com.example.EnlazadosTW.dtos.ProfessionalContactSummaryDto;
import com.example.EnlazadosTW.entities.AttendanceRecord;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.enums.AttendanceRecordStatus;
import com.example.EnlazadosTW.repositories.AttendanceRecordRepository;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.ProfessionalProfileRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio CRUD para registros administrativos de asistencia.
 */
@Service
@Transactional
public class AttendanceRecordService {

	private final AttendanceRecordRepository attendanceRecordRepository;
	private final ProfessionalProfileRepository professionalProfileRepository;
	private final PatientRepository patientRepository;

	public AttendanceRecordService(
		AttendanceRecordRepository attendanceRecordRepository,
		ProfessionalProfileRepository professionalProfileRepository,
		PatientRepository patientRepository
	) {
		this.attendanceRecordRepository = attendanceRecordRepository;
		this.professionalProfileRepository = professionalProfileRepository;
		this.patientRepository = patientRepository;
	}

	public AttendanceRecordResponseDto createAttendanceRecord(AttendanceRecordCreateDto createDto) {
		ProfessionalProfile professionalProfile = getProfessionalProfileById(createDto.professionalId());
		Patient patient = getPatientById(createDto.patientId());

		AttendanceRecord attendanceRecord = AttendanceRecord.builder()
			.professionalProfile(professionalProfile)
			.patient(patient)
			.sessionDate(createDto.sessionDate())
			.sessionFeeSnapshot(createDto.sessionFeeSnapshot())
			.healthInsuranceName(createDto.healthInsuranceName().trim())
			.healthInsuranceCoverageAmount(createDto.healthInsuranceCoverageAmount())
			.copaymentAmount(createDto.copaymentAmount())
			.notes(normalizeText(createDto.notes()))
			.status(AttendanceRecordStatus.PENDING)
			.build();

		return mapToResponseDto(attendanceRecordRepository.save(attendanceRecord));
	}

	@Transactional(readOnly = true)
	public AttendanceRecordResponseDto getAttendanceRecordById(UUID id) {
		return mapToResponseDto(getAttendanceRecordEntityById(id));
	}

	@Transactional(readOnly = true)
	public List<AttendanceRecordResponseDto> getAllAttendanceRecords() {
		return attendanceRecordRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AttendanceRecordResponseDto> getAttendanceRecordsByProfessionalId(UUID professionalId) {
		return attendanceRecordRepository.findByProfessionalProfileIdOrderBySessionDateDesc(professionalId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AttendanceRecordResponseDto> getAttendanceRecordsByPatientId(UUID patientId) {
		return attendanceRecordRepository.findByPatientIdOrderBySessionDateDesc(patientId)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AttendanceRecordResponseDto> getAttendanceRecordsByStatus(AttendanceRecordStatus status) {
		return attendanceRecordRepository.findByStatusOrderBySessionDateDesc(status)
			.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	public AttendanceRecordResponseDto updateAttendanceRecord(UUID id, AttendanceRecordUpdateDto updateDto) {
		AttendanceRecord attendanceRecord = getAttendanceRecordEntityById(id);

		if (updateDto.professionalId() != null) {
			attendanceRecord.setProfessionalProfile(getProfessionalProfileById(updateDto.professionalId()));
		}

		if (updateDto.patientId() != null) {
			attendanceRecord.setPatient(getPatientById(updateDto.patientId()));
		}

		if (updateDto.sessionDate() != null) {
			attendanceRecord.setSessionDate(updateDto.sessionDate());
		}

		if (updateDto.sessionFeeSnapshot() != null) {
			attendanceRecord.setSessionFeeSnapshot(updateDto.sessionFeeSnapshot());
		}

		if (updateDto.healthInsuranceName() != null) {
			attendanceRecord.setHealthInsuranceName(updateDto.healthInsuranceName().trim());
		}

		if (updateDto.healthInsuranceCoverageAmount() != null) {
			attendanceRecord.setHealthInsuranceCoverageAmount(updateDto.healthInsuranceCoverageAmount());
		}

		if (updateDto.copaymentAmount() != null) {
			attendanceRecord.setCopaymentAmount(updateDto.copaymentAmount());
		}

		if (updateDto.notes() != null) {
			attendanceRecord.setNotes(normalizeText(updateDto.notes()));
		}

		if (updateDto.status() != null) {
			attendanceRecord.setStatus(updateDto.status());
		}

		return mapToResponseDto(attendanceRecordRepository.save(attendanceRecord));
	}

	public void deleteAttendanceRecord(UUID id) {
		attendanceRecordRepository.delete(getAttendanceRecordEntityById(id));
	}

	private AttendanceRecord getAttendanceRecordEntityById(UUID id) {
		return attendanceRecordRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Registro de asistencia no encontrado con ID: " + id));
	}

	private ProfessionalProfile getProfessionalProfileById(UUID professionalId) {
		return professionalProfileRepository.findById(professionalId)
			.orElseThrow(() -> new IllegalArgumentException("Perfil profesional no encontrado con ID: " + professionalId));
	}

	private Patient getPatientById(UUID patientId) {
		return patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + patientId));
	}

	private AttendanceRecordResponseDto mapToResponseDto(AttendanceRecord attendanceRecord) {
		ProfessionalProfile professionalProfile = attendanceRecord.getProfessionalProfile();
		ProfessionalContactSummaryDto professionalDto = new ProfessionalContactSummaryDto(
			professionalProfile.getId(),
			professionalProfile.getUser().getId(),
			professionalProfile.getUser().getFirstName(),
			professionalProfile.getUser().getLastName(),
			professionalProfile.getSpecialty(),
			professionalProfile.getAcceptedHealthInsurances(),
			professionalProfile.getSessionFee()
		);

		Patient patient = attendanceRecord.getPatient();
		PatientBasicDto patientDto = new PatientBasicDto(
			patient.getId(),
			patient.getFirstName(),
			patient.getLastName(),
			patient.getDiagnosis()
		);

		return new AttendanceRecordResponseDto(
			attendanceRecord.getId(),
			professionalDto,
			patientDto,
			attendanceRecord.getSessionDate(),
			attendanceRecord.getSessionFeeSnapshot(),
			attendanceRecord.getHealthInsuranceName(),
			attendanceRecord.getHealthInsuranceCoverageAmount(),
			attendanceRecord.getCopaymentAmount(),
			attendanceRecord.getNotes(),
			attendanceRecord.getStatus(),
			attendanceRecord.getCreatedAt(),
			attendanceRecord.getUpdatedAt()
		);
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}

		String trimmedValue = value.trim();
		return trimmedValue.isBlank() ? null : trimmedValue;
	}
}
