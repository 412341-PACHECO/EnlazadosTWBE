package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.AttendanceRecord;
import com.example.EnlazadosTW.enums.AttendanceRecordStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para registros de asistencia.
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

	List<AttendanceRecord> findByProfessionalProfileIdOrderBySessionDateDesc(UUID professionalId);

	List<AttendanceRecord> findByPatientIdOrderBySessionDateDesc(UUID patientId);

	List<AttendanceRecord> findByStatusOrderBySessionDateDesc(AttendanceRecordStatus status);

	List<AttendanceRecord> findByAttendanceBillingIdOrderBySessionDateAsc(UUID billingId);
}
