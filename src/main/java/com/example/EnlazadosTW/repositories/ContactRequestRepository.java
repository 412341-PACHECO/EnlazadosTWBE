package com.example.EnlazadosTW.repositories;

import com.example.EnlazadosTW.entities.ContactRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para solicitudes de contacto.
 */
@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, UUID> {

	List<ContactRequest> findByProfessionalProfileIdOrderByCreatedAtDesc(UUID professionalProfileId);

	List<ContactRequest> findByParentUserIdOrderByCreatedAtDesc(UUID parentUserId);

	List<ContactRequest> findByProfessionalProfileIdAndStatus(UUID professionalProfileId, com.example.EnlazadosTW.enums.ContactRequestStatus status);
}
