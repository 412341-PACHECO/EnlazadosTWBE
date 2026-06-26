package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.TherapeuticTeamInvitation;
import com.example.EnlazadosTW.enums.TherapeuticTeamInvitationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para invitaciones a equipos terapeuticos.
 */
@Repository
public interface TherapeuticTeamInvitationRepository extends JpaRepository<TherapeuticTeamInvitation, UUID> {

	Optional<TherapeuticTeamInvitation> findByToken(String token);

	List<TherapeuticTeamInvitation> findByPatientId(UUID patientId);

	List<TherapeuticTeamInvitation> findByPatientIdAndInvitedEmailIgnoreCaseAndStatus(
		UUID patientId,
		String invitedEmail,
		TherapeuticTeamInvitationStatus status
	);
}
