package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.Institution;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para gestionar instituciones.
 */
public interface InstitutionRepository extends JpaRepository<Institution, UUID> {

	List<Institution> findByType(String type);

	List<Institution> findByNameContainingIgnoreCase(String name);
}
