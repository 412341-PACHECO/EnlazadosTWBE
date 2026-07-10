package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.ContactRequestCreateDto;
import com.example.EnlazadosTW.dtos.ContactRequestResponseDto;
import com.example.EnlazadosTW.services.ContactRequestService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para solicitudes de contacto entre tutores y profesionales.
 */
@RestController
@RequestMapping("/api/contact-requests")
public class ContactRequestController {

	private final ContactRequestService contactRequestService;

	public ContactRequestController(ContactRequestService contactRequestService) {
		this.contactRequestService = contactRequestService;
	}

	@PostMapping
	public ResponseEntity<ContactRequestResponseDto> createContactRequest(
		@Valid @RequestBody ContactRequestCreateDto createDto
	) {
		ContactRequestResponseDto response = contactRequestService.createContactRequest(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/search/by-professional")
	public ResponseEntity<List<ContactRequestResponseDto>> getContactRequestsByProfessional(
		@RequestParam UUID professionalId
	) {
		List<ContactRequestResponseDto> responses = contactRequestService.getContactRequestsByProfessional(professionalId);
		return ResponseEntity.ok(responses);
	}
}
