package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.PasswordResetToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

	Optional<PasswordResetToken> findByToken(String token);

	List<PasswordResetToken> findByUserIdAndUsedAtIsNull(UUID userId);
}
