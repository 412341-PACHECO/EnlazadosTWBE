package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio utilitario para resolver el usuario autenticado actual.
 */
@Service
@Transactional(readOnly = true)
public class CurrentUserService {

	private final UserRepository userRepository;

	public CurrentUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getCurrentAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || authentication.getName() == null) {
			throw new IllegalArgumentException("No hay un usuario autenticado en la solicitud");
		}

		return userRepository.findByEmail(authentication.getName())
			.orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));
	}

	public void requireRole(User user, String expectedRole) {
		String normalizedRole = normalizeRoleName(user.getRole().getName());
		if (!normalizedRole.equalsIgnoreCase(expectedRole)) {
			throw new IllegalArgumentException("El usuario no tiene permisos para realizar esta operacion");
		}
	}

	private String normalizeRoleName(String roleName) {
		String upper = roleName.toUpperCase();
		return upper.startsWith("ROLE_") ? upper.substring(5) : upper;
	}
}
