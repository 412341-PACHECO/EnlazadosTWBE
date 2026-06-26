package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.ParentProfileResponseDto;
import com.example.EnlazadosTW.dtos.PatientBasicDto;
import com.example.EnlazadosTW.dtos.RoleBasicDto;
import com.example.EnlazadosTW.dtos.UserCreateDto;
import com.example.EnlazadosTW.dtos.UserResponseDto;
import com.example.EnlazadosTW.dtos.UserUpdateDto;
import com.example.EnlazadosTW.entities.EmailVerificationToken;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.Role;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.repositories.PatientRepository;
import com.example.EnlazadosTW.repositories.RoleRepository;
import com.example.EnlazadosTW.repositories.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PatientRepository patientRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationTokenService emailVerificationTokenService;
	private final EmailService emailService;

	public UserService(
		UserRepository userRepository,
		RoleRepository roleRepository,
		PatientRepository patientRepository,
		PasswordEncoder passwordEncoder,
		EmailVerificationTokenService emailVerificationTokenService,
		EmailService emailService
	) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.patientRepository = patientRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailVerificationTokenService = emailVerificationTokenService;
		this.emailService = emailService;
	}

	public UserResponseDto createUser(UserCreateDto createDto) {
		if (userRepository.existsByEmail(createDto.email())) {
			throw new IllegalArgumentException("Ya existe un usuario con el email: " + createDto.email());
		}

		Role role = roleRepository.findById(createDto.roleId())
			.orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + createDto.roleId()));

		User user = User.builder()
			.email(createDto.email())
			.password(passwordEncoder.encode(createDto.password()))
			.firstName(createDto.firstName())
			.lastName(createDto.lastName())
			.role(role)
			.isActive(true)
			.enabled(false)
			.build();

		User savedUser = userRepository.save(user);
		EmailVerificationToken verificationToken = emailVerificationTokenService.createForUser(savedUser);
		emailService.sendVerificationEmail(savedUser, verificationToken.getToken());

		return mapToResponseDto(savedUser);
	}

	@Transactional(readOnly = true)
	public UserResponseDto getUserById(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		return mapToResponseDto(user);
	}

	@Transactional(readOnly = true)
	public UserResponseDto getUserByEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

		return mapToResponseDto(user);
	}

	@Transactional(readOnly = true)
	public ParentProfileResponseDto getParentProfileById(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		validateParentRole(user);
		return mapToParentProfileResponseDto(user);
	}

	@Transactional(readOnly = true)
	public ParentProfileResponseDto getParentProfileByEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

		validateParentRole(user);
		return mapToParentProfileResponseDto(user);
	}

	@Transactional(readOnly = true)
	public List<UserResponseDto> getAllUsers() {
		return userRepository.findAll()
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<UserResponseDto> getActiveUsers() {
		return userRepository.findByIsActive(true)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<UserResponseDto> getUsersByRole(UUID roleId) {
		return userRepository.findByRoleId(roleId)
			.stream()
			.map(this::mapToResponseDto)
			.collect(Collectors.toList());
	}

	public UserResponseDto updateUser(UUID id, UserUpdateDto updateDto) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		if (updateDto.email() != null && !updateDto.email().equals(user.getEmail())) {
			if (userRepository.existsByEmail(updateDto.email())) {
				throw new IllegalArgumentException("Ya existe un usuario con el email: " + updateDto.email());
			}
			user.setEmail(updateDto.email());
		}

		if (updateDto.firstName() != null) {
			user.setFirstName(updateDto.firstName());
		}

		if (updateDto.lastName() != null) {
			user.setLastName(updateDto.lastName());
		}

		if (updateDto.password() != null) {
			user.setPassword(passwordEncoder.encode(updateDto.password()));
		}

		if (updateDto.isActive() != null) {
			user.setIsActive(updateDto.isActive());
		}

		User updatedUser = userRepository.save(user);
		return mapToResponseDto(updatedUser);
	}

	public UserResponseDto deactivateUser(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		user.setIsActive(false);
		User deactivatedUser = userRepository.save(user);
		return mapToResponseDto(deactivatedUser);
	}

	public UserResponseDto activateUser(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

		user.setIsActive(true);
		User activatedUser = userRepository.save(user);
		return mapToResponseDto(activatedUser);
	}

	private UserResponseDto mapToResponseDto(User user) {
		return new UserResponseDto(
			user.getId(),
			user.getEmail(),
			user.getFirstName(),
			user.getLastName(),
			user.getFcmToken(),
			user.getIsActive(),
			user.getEnabled(),
			new RoleBasicDto(user.getRole().getId(), user.getRole().getName()),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}

	private ParentProfileResponseDto mapToParentProfileResponseDto(User user) {
		List<PatientBasicDto> patients = patientRepository.findByParentId(user.getId())
			.stream()
			.map(this::mapToPatientBasicDto)
			.collect(Collectors.toList());

		return new ParentProfileResponseDto(
			user.getId(),
			user.getEmail(),
			user.getFirstName(),
			user.getLastName(),
			user.getIsActive(),
			user.getEnabled(),
			new RoleBasicDto(user.getRole().getId(), user.getRole().getName()),
			patients,
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}

	private PatientBasicDto mapToPatientBasicDto(Patient patient) {
		return new PatientBasicDto(
			patient.getId(),
			patient.getFirstName(),
			patient.getLastName(),
			patient.getDiagnosis()
		);
	}

	private void validateParentRole(User user) {
		String roleName = normalizeRoleName(user.getRole().getName());
		if (!"PARENT".equalsIgnoreCase(roleName)) {
			throw new IllegalArgumentException("El usuario no tiene rol PARENT");
		}
	}

	private String normalizeRoleName(String roleName) {
		String upper = roleName.toUpperCase();
		return upper.startsWith("ROLE_") ? upper.substring(5) : upper;
	}
}
