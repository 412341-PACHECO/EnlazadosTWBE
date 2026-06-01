package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.AuthResponseDto;
import com.example.EnlazadosTW.dtos.LoginRequestDto;
import com.example.EnlazadosTW.exceptions.InvalidCredentialsException;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.repositories.UserRepository;
import com.example.EnlazadosTW.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Servicio de autenticación que maneja el login de usuarios.
 * Valida credenciales y genera tokens JWT.
 */
@Service
public class AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService userDetailsService;

	public AuthenticationService(UserRepository userRepository,
								 PasswordEncoder passwordEncoder,
								 JwtTokenProvider jwtTokenProvider,
								 CustomUserDetailsService userDetailsService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Autentica un usuario validando sus credenciales y generando un token JWT.
	 *
	 * @param loginRequest solicitud con email y contraseña
	 * @return respuesta con token JWT y refresh token
	 * @throws InvalidCredentialsException si las credenciales son inválidas
	 * @throws UserNotFoundException si el usuario no existe (lanzado por CustomUserDetailsService)
	 * @throws UserInactiveException si el usuario está inactivo (lanzado por CustomUserDetailsService)
	 */
	public AuthResponseDto authenticate(LoginRequestDto loginRequest) {
		// Buscar usuario por email y validar contraseña
		User user = userRepository.findByEmail(loginRequest.email())
			.orElseThrow(() -> new InvalidCredentialsException(
				"Email o contraseña incorrectos"
			));

		// Validar contraseña
		if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
			throw new InvalidCredentialsException(
				"Email o contraseña incorrectos"
			);
		}

		// Cargar UserDetails (esto lanzará UserInactiveException si el usuario está inactivo)
		UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.email());

		// Generar tokens
		String accessToken = jwtTokenProvider.generateToken(userDetails);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

		return new AuthResponseDto(
			accessToken,
			refreshToken,
			3600L, // 1 hora en segundos
			user.getEmail(),
			user.getRole().getName()
		);
	}

	/**
	 * Refresca el access token usando un refresh token válido.
	 *
	 * @param refreshToken el refresh token proporcionado por el cliente
	 * @return respuesta con nuevo access token
	 * @throws TokenExpiredException si el refresh token ha expirado
	 * @throws InvalidTokenException si el refresh token es inválido
	 */
	public AuthResponseDto refreshAccessToken(String refreshToken) {
		// Validar el refresh token (lanza excepciones si no es válido)
		jwtTokenProvider.validateToken(refreshToken);

		// Extraer email del refresh token
		String email = jwtTokenProvider.getEmailFromToken(refreshToken);

		// Cargar usuario y generar nuevo access token
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		String accessToken = jwtTokenProvider.generateToken(userDetails);

		User user = userRepository.findByEmail(email).orElseThrow();

		return new AuthResponseDto(
			accessToken,
			refreshToken,
			3600L,
			user.getEmail(),
			user.getRole().getName()
		);
	}
}
