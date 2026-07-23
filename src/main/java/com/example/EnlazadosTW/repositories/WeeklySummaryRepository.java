package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.WeeklySummary;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar resúmenes semanales generados por el sistema.
 */
@Repository
public interface WeeklySummaryRepository extends JpaRepository<WeeklySummary, UUID> {

	List<WeeklySummary> findByPatientIdOrderByWeekStartDesc(UUID patientId);

	Optional<WeeklySummary> findByPatientIdAndWeekStartAndWeekEnd(
		UUID patientId,
		LocalDate weekStart,
		LocalDate weekEnd
	);

	Optional<WeeklySummary> findTopByPatientIdOrderByWeekStartDesc(UUID patientId);
}
