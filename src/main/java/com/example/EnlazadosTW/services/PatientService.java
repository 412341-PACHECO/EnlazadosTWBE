package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.InstitutionBasicDto;
import com.example.EnlazadosTW.dtos.PatientCreateDto;
import com.example.EnlazadosTW.dtos.PatientResponseDto;
import com.example.EnlazadosTW.dtos.PatientUpdateDto;
import com.example.EnlazadosTW.dtos.UserBasicDto;
import com.example.EnlazadosTW.entities.Institution;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.repositories.InstitutionRepository;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar pacientes.
 * Maneja la lógica de negocio para crear, actualizar, buscar y eliminar pacientes.
 */
@Service
@Transactional
public class PatientService {

	private final PatientRepository patientRepository;
	private final UserRepository userRepository;
	private final InstitutionRepository institutionRepository;

	public PatientService(PatientRepository patientRepository,
						 UserRepository userRepository,
						 InstitutionRepository institutionRepository) {
		this.patientRepository = patientRepository;
		this.userRepository = userRepository;
		this.institutionRepository = institutionRepository;
	}

	/**
	 * Crea un nuevo paciente.
	 *
	 * @param createDto datos del paciente a crear
	 * @return paciente creado
	 */
	public PatientResponseDto createPatient(PatientCreateDto createDto) {
		Patient patient = Patient.builder()
			.firstName(createDto.firstName())
			.lastName(createDto.lastName())
			.diagnosis(createDto.diagnosis())
			.parent(getParentIfPresent(createDto.parentId()))
			.institution(getInstitutionIfPresent(createDto.institutionId()))
			.build();

		Patient savedPatient = patientRepository.save(patient);
		return mapToResponseDto(savedPatient);
	}

	/**
	 * Obtiene un paciente por su ID.
	 *
	 * @param id ID del paciente
	 * @return paciente encontrado
	 */
	@Transactional(readOnly = true)
	public PatientResponseDto getPatientById(UUID id) {
		Patient patient = patientRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + id));

		return mapToResponseDto(patient);
	}

	/**
	 * Obtiene todos los pacientes.
	 *
	 * @return lista de pacientes
	 */
	@Transactional(readOnly = true)
	public List<PatientResponseDto> getAllPatients() {
		return patientRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Obtiene todos los pacientes de un padre/tutor.
	 *
	 * @param parentId ID del padre/tutor
	 * @return lista de pacientes
	 */
	@Transactional(readOnly = true)
	public List<PatientResponseDto> getPatientsByParentId(UUID parentId) {
		return patientRepository.findByParentId(parentId)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Obtiene todos los pacientes de una institución.
	 *
	 * @param institutionId ID de la institución
	 * @return lista de pacientes
	 */
	@Transactional(readOnly = true)
	public List<PatientResponseDto> getPatientsByInstitutionId(UUID institutionId) {
		return patientRepository.findByInstitutionId(institutionId)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Actualiza un paciente existente.
	 *
	 * @param id ID del paciente
	 * @param updateDto datos a actualizar
	 * @return paciente actualizado
	 */
	public PatientResponseDto updatePatient(UUID id, PatientUpdateDto updateDto) {
		Patient patient = patientRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + id));

		if (updateDto.firstName() != null) {
			patient.setFirstName(updateDto.firstName());
		}

		if (updateDto.lastName() != null) {
			patient.setLastName(updateDto.lastName());
		}

		if (updateDto.diagnosis() != null) {
			patient.setDiagnosis(updateDto.diagnosis());
		}

		if (updateDto.parentId() != null) {
			patient.setParent(getParentIfPresent(updateDto.parentId()));
		}

		if (updateDto.institutionId() != null) {
			patient.setInstitution(getInstitutionIfPresent(updateDto.institutionId()));
		}

		Patient updatedPatient = patientRepository.save(patient);
		return mapToResponseDto(updatedPatient);
	}

	/**
	 * Elimina un paciente.
	 *
	 * @param id ID del paciente
	 */
	public void deletePatient(UUID id) {
		Patient patient = patientRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + id));

		patientRepository.delete(patient);
	}

	private User getParentIfPresent(UUID parentId) {
		if (parentId == null) {
			return null;
		}

		return userRepository.findById(parentId)
			.orElseThrow(() -> new IllegalArgumentException("Usuario padre no encontrado con ID: " + parentId));
	}

	private Institution getInstitutionIfPresent(UUID institutionId) {
		if (institutionId == null) {
			return null;
		}

		return institutionRepository.findById(institutionId)
			.orElseThrow(() -> new IllegalArgumentException("Institución no encontrada con ID: " + institutionId));
	}

	private PatientResponseDto mapToResponseDto(Patient patient) {
		UserBasicDto parentDto = null;
		if (patient.getParent() != null) {
			parentDto = new UserBasicDto(
				patient.getParent().getId(),
				patient.getParent().getEmail(),
				patient.getParent().getFirstName(),
				patient.getParent().getLastName()
			);
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
}
