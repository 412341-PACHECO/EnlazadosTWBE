package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.dtos.AuthResponseDto;
import com.example.EnlazadosTW.dtos.ForgotPasswordRequestDto;
import com.example.EnlazadosTW.dtos.LoginRequestDto;
import com.example.EnlazadosTW.dtos.MessageResponseDto;
import com.example.EnlazadosTW.dtos.ResendVerificationEmailRequestDto;
import com.example.EnlazadosTW.dtos.ResetPasswordRequestDto;
import com.example.EnlazadosTW.entities.EmailVerificationToken;
import com.example.EnlazadosTW.entities.PasswordResetToken;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.exceptions.InvalidCredentialsException;
import com.example.EnlazadosTW.repositories.UserRepository;
import java.time.LocalDateTime;
import com.example.EnlazadosTW.security.JwtTokenProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService userDetailsService;
	private final EmailVerificationTokenService emailVerificationTokenService;
	private final PasswordResetTokenService passwordResetTokenService;
	private final EmailService emailService;

	public AuthenticationService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtTokenProvider jwtTokenProvider,
		CustomUserDetailsService userDetailsService,
		EmailVerificationTokenService emailVerificationTokenService,
		PasswordResetTokenService passwordResetTokenService,
		EmailService emailService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userDetailsService = userDetailsService;
		this.emailVerificationTokenService = emailVerificationTokenService;
		this.passwordResetTokenService = passwordResetTokenService;
		this.emailService = emailService;
	}

	public AuthResponseDto authenticate(LoginRequestDto loginRequest) {
		User user = userRepository.findByEmail(loginRequest.email())
			.orElseThrow(() -> new InvalidCredentialsException("Email o contrasena incorrectos"));

		if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
			throw new InvalidCredentialsException("Email o contrasena incorrectos");
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.email());
		String accessToken = jwtTokenProvider.generateToken(userDetails);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

		return new AuthResponseDto(
			accessToken,
			refreshToken,
			3600L,
			user.getEmail(),
			user.getRole().getName()
		);
	}

	public AuthResponseDto refreshAccessToken(String refreshToken) {
		jwtTokenProvider.validateToken(refreshToken);
		String email = jwtTokenProvider.getEmailFromToken(refreshToken);
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

	public MessageResponseDto verifyEmail(String token) {
		EmailVerificationToken verificationToken = emailVerificationTokenService.validateToken(token);
		User user = verificationToken.getUser();

		user.setEnabled(true);
		user.setEmailVerifiedAt(LocalDateTime.now());
		userRepository.save(user);
		emailVerificationTokenService.markAsUsed(verificationToken);

		return new MessageResponseDto("Cuenta verificada correctamente");
	}

	public MessageResponseDto resendVerificationEmail(ResendVerificationEmailRequestDto request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new InvalidCredentialsException("No existe una cuenta asociada al email indicado"));

		if (Boolean.TRUE.equals(user.getEnabled())) {
			return new MessageResponseDto("La cuenta ya se encuentra verificada");
		}

		EmailVerificationToken verificationToken = emailVerificationTokenService.createForUser(user);
		emailService.sendVerificationEmail(user, verificationToken.getToken());

		return new MessageResponseDto("Se envio un nuevo correo de verificacion");
	}

	public MessageResponseDto requestPasswordReset(ForgotPasswordRequestDto request) {
		userRepository.findByEmail(request.email()).ifPresent(user -> {
			if (Boolean.TRUE.equals(user.getEnabled()) && Boolean.TRUE.equals(user.getIsActive())) {
				PasswordResetToken resetToken = passwordResetTokenService.createForUser(user);
				emailService.sendPasswordResetEmail(user, resetToken.getToken());
			}
		});

		return new MessageResponseDto("Si el email existe, recibira instrucciones para cambiar la contrasena");
	}

	public MessageResponseDto resetPassword(ResetPasswordRequestDto request) {
		PasswordResetToken resetToken = passwordResetTokenService.validateToken(request.token());
		User user = resetToken.getUser();

		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);

		passwordResetTokenService.markAsUsed(resetToken);
		passwordResetTokenService.invalidateActiveTokens(user.getId());

		return new MessageResponseDto("La contrasena fue actualizada correctamente");
	}
}
