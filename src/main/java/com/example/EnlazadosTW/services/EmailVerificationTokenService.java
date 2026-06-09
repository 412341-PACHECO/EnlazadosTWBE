package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.entities.EmailVerificationToken;
import com.example.EnlazadosTW.entities.User;
import com.example.EnlazadosTW.exceptions.InvalidTokenException;
import com.example.EnlazadosTW.exceptions.TokenExpiredException;
import com.example.EnlazadosTW.repositories.EmailVerificationTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmailVerificationTokenService {

	private final EmailVerificationTokenRepository emailVerificationTokenRepository;
	private final long verificationExpirationMinutes;

	public EmailVerificationTokenService(
		EmailVerificationTokenRepository emailVerificationTokenRepository,
		@Value("${app.email.verification-expiration-minutes:1440}") long verificationExpirationMinutes
	) {
		this.emailVerificationTokenRepository = emailVerificationTokenRepository;
		this.verificationExpirationMinutes = verificationExpirationMinutes;
	}

	public EmailVerificationToken createForUser(User user) {
		invalidateActiveTokens(user.getId());

		EmailVerificationToken token = EmailVerificationToken.builder()
			.token(UUID.randomUUID().toString())
			.user(user)
			.expiresAt(LocalDateTime.now().plusMinutes(verificationExpirationMinutes))
			.build();

		return emailVerificationTokenRepository.save(token);
	}

	@Transactional(readOnly = true)
	public EmailVerificationToken validateToken(String rawToken) {
		EmailVerificationToken token = emailVerificationTokenRepository.findByToken(rawToken)
			.orElseThrow(() -> new InvalidTokenException("El token de verificacion no es valido"));

		if (token.getUsedAt() != null) {
			throw new InvalidTokenException("El token de verificacion ya fue utilizado");
		}

		if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new TokenExpiredException("El token de verificacion ha expirado");
		}

		return token;
	}

	public void markAsUsed(EmailVerificationToken token) {
		token.setUsedAt(LocalDateTime.now());
		emailVerificationTokenRepository.save(token);
	}

	public void invalidateActiveTokens(UUID userId) {
		List<EmailVerificationToken> activeTokens = emailVerificationTokenRepository.findByUserIdAndUsedAtIsNull(userId);
		if (activeTokens.isEmpty()) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		activeTokens.forEach(token -> token.setUsedAt(now));
		emailVerificationTokenRepository.saveAll(activeTokens);
	}
}
