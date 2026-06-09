package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.Patient;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para gestionar pacientes.
 */
public interface PatientRepository extends JpaRepository<Patient, UUID> {

	List<Patient> findByParentId(UUID parentId);

	List<Patient> findByInstitutionId(UUID institutionId);
}
