package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.WeeklySummaryGenerateDto;
import com.example.EnlazadosTW.dtos.WeeklySummaryResponseDto;
import com.example.EnlazadosTW.services.WeeklySummaryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para resúmenes semanales automáticos del legajo.
 */
@RestController
@RequestMapping("/api/weekly-summaries")
public class WeeklySummaryController {

	private final WeeklySummaryService weeklySummaryService;

	public WeeklySummaryController(WeeklySummaryService weeklySummaryService) {
		this.weeklySummaryService = weeklySummaryService;
	}

	@PostMapping("/generate")
	public ResponseEntity<WeeklySummaryResponseDto> generateWeeklySummary(
		@Valid @RequestBody WeeklySummaryGenerateDto generateDto
	) {
		WeeklySummaryResponseDto response = weeklySummaryService.generateWeeklySummary(generateDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<WeeklySummaryResponseDto> getWeeklySummaryById(@PathVariable UUID id) {
		return ResponseEntity.ok(weeklySummaryService.getWeeklySummaryById(id));
	}

	@GetMapping("/search/by-patient")
	public ResponseEntity<List<WeeklySummaryResponseDto>> getWeeklySummariesByPatientId(@RequestParam UUID patientId) {
		return ResponseEntity.ok(weeklySummaryService.getWeeklySummariesByPatientId(patientId));
	}

	@GetMapping("/search/latest-by-patient")
	public ResponseEntity<WeeklySummaryResponseDto> getLatestWeeklySummaryByPatientId(@RequestParam UUID patientId) {
		return ResponseEntity.ok(weeklySummaryService.getLatestWeeklySummaryByPatientId(patientId));
	}
}
