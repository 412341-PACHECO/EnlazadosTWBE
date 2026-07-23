package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.DailyReport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar reportes diarios.
 */
@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, UUID> {

	List<DailyReport> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

	List<DailyReport> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

	List<DailyReport> findByPatientIdAndAuthorIdOrderByCreatedAtDesc(UUID patientId, UUID authorId);

	List<DailyReport> findByPatientIdAndCreatedAtBetweenOrderByCreatedAtAsc(
		UUID patientId,
		LocalDateTime startDateTime,
		LocalDateTime endDateTime
	);

	List<DailyReport> findByCreatedAtBetweenOrderByCreatedAtAsc(
		LocalDateTime startDateTime,
		LocalDateTime endDateTime
	);
}
