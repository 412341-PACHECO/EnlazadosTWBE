package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.EmailVerificationToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

	Optional<EmailVerificationToken> findByToken(String token);

	List<EmailVerificationToken> findByUserIdAndUsedAtIsNull(UUID userId);
}
