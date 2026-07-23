package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.AttendanceBilling;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para cabeceras de liquidacion de asistencias.
 */
@Repository
public interface AttendanceBillingRepository extends JpaRepository<AttendanceBilling, UUID> {

	List<AttendanceBilling> findByProfessionalProfileIdOrderByCreatedAtDesc(UUID professionalId);

	List<AttendanceBilling> findByBillingPeriodOrderByCreatedAtDesc(String billingPeriod);
}
