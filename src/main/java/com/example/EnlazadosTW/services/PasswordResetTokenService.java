package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.entities.PasswordResetToken;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.exceptions.InvalidTokenException;
import com.example.EnlazadosTW.exceptions.TokenExpiredException;
import com.example.EnlazadosTW.repositories.PasswordResetTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PasswordResetTokenService {

	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final long passwordResetExpirationMinutes;

	public PasswordResetTokenService(
		PasswordResetTokenRepository passwordResetTokenRepository,
		@Value("${app.email.password-reset-expiration-minutes:30}") long passwordResetExpirationMinutes
	) {
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.passwordResetExpirationMinutes = passwordResetExpirationMinutes;
	}

	public PasswordResetToken createForUser(User user) {
		invalidateActiveTokens(user.getId());

		PasswordResetToken token = PasswordResetToken.builder()
			.token(UUID.randomUUID().toString())
			.user(user)
			.expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes))
			.build();

		return passwordResetTokenRepository.save(token);
	}

	@Transactional(readOnly = true)
	public PasswordResetToken validateToken(String rawToken) {
		PasswordResetToken token = passwordResetTokenRepository.findByToken(rawToken)
			.orElseThrow(() -> new InvalidTokenException("El token de reseteo no es valido"));

		if (token.getUsedAt() != null) {
			throw new InvalidTokenException("El token de reseteo ya fue utilizado");
		}

		if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new TokenExpiredException("El token de reseteo ha expirado");
		}

		return token;
	}

	public void markAsUsed(PasswordResetToken token) {
		token.setUsedAt(LocalDateTime.now());
		passwordResetTokenRepository.save(token);
	}

	public void invalidateActiveTokens(UUID userId) {
		List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findByUserIdAndUsedAtIsNull(userId);
		if (activeTokens.isEmpty()) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		activeTokens.forEach(token -> token.setUsedAt(now));
		passwordResetTokenRepository.saveAll(activeTokens);
	}
}
