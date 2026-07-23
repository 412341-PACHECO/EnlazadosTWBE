package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.AttendanceBillingGenerateDto;
import com.example.EnlazadosTW.dtos.AttendanceBillingResponseDto;
import com.example.EnlazadosTW.dtos.AttendanceBillingStatusUpdateDto;
import com.example.EnlazadosTW.services.AttendanceBillingService;
import com.example.EnlazadosTW.services.PdfBillingReportService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para liquidaciones administrativas de asistencias.
 */
@RestController
@RequestMapping("/api/attendance-billings")
public class AttendanceBillingController {

	private final AttendanceBillingService attendanceBillingService;
	private final PdfBillingReportService pdfBillingReportService;

	public AttendanceBillingController(
		AttendanceBillingService attendanceBillingService,
		PdfBillingReportService pdfBillingReportService
	) {
		this.attendanceBillingService = attendanceBillingService;
		this.pdfBillingReportService = pdfBillingReportService;
	}

	@PostMapping("/generate")
	public ResponseEntity<AttendanceBillingResponseDto> generateAttendanceBilling(
		@Valid @RequestBody AttendanceBillingGenerateDto generateDto
	) {
		AttendanceBillingResponseDto response = attendanceBillingService.generateAttendanceBilling(generateDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AttendanceBillingResponseDto> getAttendanceBillingById(@PathVariable UUID id) {
		return ResponseEntity.ok(attendanceBillingService.getAttendanceBillingById(id));
	}

	@GetMapping
	public ResponseEntity<List<AttendanceBillingResponseDto>> getAllAttendanceBillings() {
		return ResponseEntity.ok(attendanceBillingService.getAllAttendanceBillings());
	}

	@GetMapping("/search/by-professional")
	public ResponseEntity<List<AttendanceBillingResponseDto>> getAttendanceBillingsByProfessionalId(
		@RequestParam UUID professionalId,
		@RequestParam(required = false) LocalDate dateFrom,
		@RequestParam(required = false) LocalDate dateTo
	) {
		return ResponseEntity.ok(
			attendanceBillingService.getAttendanceBillingsByProfessionalId(professionalId, dateFrom, dateTo)
		);
	}

	@GetMapping("/search/by-period")
	public ResponseEntity<List<AttendanceBillingResponseDto>> getAttendanceBillingsByPeriod(
		@RequestParam String billingPeriod,
		@RequestParam(required = false) LocalDate dateFrom,
		@RequestParam(required = false) LocalDate dateTo
	) {
		return ResponseEntity.ok(
			attendanceBillingService.getAttendanceBillingsByPeriod(billingPeriod, dateFrom, dateTo)
		);
	}

	@GetMapping("/{id}/pdf")
	public ResponseEntity<byte[]> downloadAttendanceBillingPdf(@PathVariable UUID id) {
		byte[] pdfBytes = pdfBillingReportService.generateAttendanceBillingPdf(id);

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance-billing-" + id + ".pdf")
			.contentType(MediaType.APPLICATION_PDF)
			.body(pdfBytes);
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<AttendanceBillingResponseDto> updateAttendanceBillingStatus(
		@PathVariable UUID id,
		@Valid @RequestBody AttendanceBillingStatusUpdateDto updateDto
	) {
		AttendanceBillingResponseDto response = attendanceBillingService.updateAttendanceBillingStatus(id, updateDto);
		return ResponseEntity.ok(response);
	}
}
