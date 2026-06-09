package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio para gestionar instituciones.
 */
public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
}
