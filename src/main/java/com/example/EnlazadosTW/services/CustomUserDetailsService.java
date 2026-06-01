package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.exceptions.UserInactiveException;
import com.example.EnlazadosTW.exceptions.UserNotFoundException;
import com.example.EnlazadosTW.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio personalizado para cargar detalles del usuario desde la base de datos.
 * Implementa UserDetailsService de Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Carga un usuario por su email (que actúa como username).
	 *
	 * @param email email del usuario
	 * @return UserDetails con información del usuario y su rol
	 * @throws UsernameNotFoundException si el usuario no existe
	 */
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + email));

		// Verificar que el usuario está activo
		if (!user.getIsActive()) {
			throw new UserInactiveException("El usuario está inactivo: " + email + ". Contacte con administración");
		}

		// Construir UserDetails con rol como autoridad
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
			"ROLE_" + user.getRole().getName().toUpperCase()
		);

		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getEmail())
			.password(user.getPassword())
			.authorities(Collections.singletonList(authority))
			.accountLocked(false)
			.accountExpired(false)
			.credentialsExpired(false)
			.disabled(false)
			.build();
	}

}
