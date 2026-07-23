package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.AttendanceRecordCreateDto;
import com.example.EnlazadosTW.dtos.AttendanceRecordResponseDto;
import com.example.EnlazadosTW.dtos.AttendanceRecordUpdateDto;
import com.example.EnlazadosTW.enums.AttendanceRecordStatus;
import com.example.EnlazadosTW.services.AttendanceRecordService;
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
 * Controlador REST para registros administrativos de asistencia.
 */
@RestController
@RequestMapping("/api/attendance-records")
public class AttendanceRecordController {

	private final AttendanceRecordService attendanceRecordService;

	public AttendanceRecordController(AttendanceRecordService attendanceRecordService) {
		this.attendanceRecordService = attendanceRecordService;
	}

	@PostMapping
	public ResponseEntity<AttendanceRecordResponseDto> createAttendanceRecord(
		@Valid @RequestBody AttendanceRecordCreateDto createDto
	) {
		AttendanceRecordResponseDto response = attendanceRecordService.createAttendanceRecord(createDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AttendanceRecordResponseDto> getAttendanceRecordById(@PathVariable UUID id) {
		return ResponseEntity.ok(attendanceRecordService.getAttendanceRecordById(id));
	}

	@GetMapping
	public ResponseEntity<List<AttendanceRecordResponseDto>> getAllAttendanceRecords() {
		return ResponseEntity.ok(attendanceRecordService.getAllAttendanceRecords());
	}

	@GetMapping("/search/by-professional")
	public ResponseEntity<List<AttendanceRecordResponseDto>> getAttendanceRecordsByProfessionalId(
		@RequestParam UUID professionalId
	) {
		return ResponseEntity.ok(attendanceRecordService.getAttendanceRecordsByProfessionalId(professionalId));
	}

	@GetMapping("/search/by-patient")
	public ResponseEntity<List<AttendanceRecordResponseDto>> getAttendanceRecordsByPatientId(
		@RequestParam UUID patientId
	) {
		return ResponseEntity.ok(attendanceRecordService.getAttendanceRecordsByPatientId(patientId));
	}

	@GetMapping("/search/by-status")
	public ResponseEntity<List<AttendanceRecordResponseDto>> getAttendanceRecordsByStatus(
		@RequestParam AttendanceRecordStatus status
	) {
		return ResponseEntity.ok(attendanceRecordService.getAttendanceRecordsByStatus(status));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AttendanceRecordResponseDto> updateAttendanceRecord(
		@PathVariable UUID id,
		@Valid @RequestBody AttendanceRecordUpdateDto updateDto
	) {
		return ResponseEntity.ok(attendanceRecordService.updateAttendanceRecord(id, updateDto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteAttendanceRecord(@PathVariable UUID id) {
		attendanceRecordService.deleteAttendanceRecord(id);
		return ResponseEntity.noContent().build();
	}
}
