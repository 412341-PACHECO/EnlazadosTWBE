package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.DailyReportCreateDto;
import com.example.EnlazadosTW.dtos.DailyReportResponseDto;
import com.example.EnlazadosTW.dtos.DailyReportUpdateDto;
import com.example.EnlazadosTW.services.DailyReportService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar reportes diarios.
 */
@RestController
@RequestMapping("/api/daily-reports")
public class DailyReportController {

	private final DailyReportService dailyReportService;

	public DailyReportController(DailyReportService dailyReportService) {
		this.dailyReportService = dailyReportService;
	}

	@PostMapping
	public ResponseEntity<DailyReportResponseDto> createDailyReport(
		@Valid @RequestBody DailyReportCreateDto createDto
	) {
		DailyReportResponseDto dailyReport = dailyReportService.createDailyReport(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(dailyReport);
	}

	@GetMapping("/{id}")
	public ResponseEntity<DailyReportResponseDto> getDailyReportById(@PathVariable UUID id) {
		return ResponseEntity.ok(dailyReportService.getDailyReportById(id));
	}

	@GetMapping
	public ResponseEntity<List<DailyReportResponseDto>> getAllDailyReports() {
		return ResponseEntity.ok(dailyReportService.getAllDailyReports());
	}

	@GetMapping("/search/by-patient")
	public ResponseEntity<List<DailyReportResponseDto>> getDailyReportsByPatientId(@RequestParam UUID patientId) {
		return ResponseEntity.ok(dailyReportService.getDailyReportsByPatientId(patientId));
	}

	@GetMapping("/search/by-author")
	public ResponseEntity<List<DailyReportResponseDto>> getDailyReportsByAuthorId(@RequestParam UUID authorId) {
		return ResponseEntity.ok(dailyReportService.getDailyReportsByAuthorId(authorId));
	}

	@GetMapping("/search/by-patient-and-author")
	public ResponseEntity<List<DailyReportResponseDto>> getDailyReportsByPatientIdAndAuthorId(
		@RequestParam UUID patientId,
		@RequestParam UUID authorId
	) {
		return ResponseEntity.ok(dailyReportService.getDailyReportsByPatientIdAndAuthorId(patientId, authorId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<DailyReportResponseDto> updateDailyReport(
		@PathVariable UUID id,
		@Valid @RequestBody DailyReportUpdateDto updateDto
	) {
		return ResponseEntity.ok(dailyReportService.updateDailyReport(id, updateDto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDailyReport(@PathVariable UUID id) {
		dailyReportService.deleteDailyReport(id);
		return ResponseEntity.noContent().build();
	}
}
