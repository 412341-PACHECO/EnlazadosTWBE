package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.TherapeuticTeam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar asignaciones de equipos terapeuticos.
 */
@Repository
public interface TherapeuticTeamRepository extends JpaRepository<TherapeuticTeam, UUID> {

	List<TherapeuticTeam> findByPatientId(UUID patientId);

	List<TherapeuticTeam> findByProfessionalId(UUID professionalId);

	List<TherapeuticTeam> findByPatientIdAndProfessionalId(UUID patientId, UUID professionalId);
}
