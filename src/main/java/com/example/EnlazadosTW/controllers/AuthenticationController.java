package com.example.EnlazadosTW.controllers;

import com.example.EnlazadosTW.dtos.AuthResponseDto;
import com.example.EnlazadosTW.dtos.ForgotPasswordRequestDto;
import com.example.EnlazadosTW.dtos.LoginRequestDto;
import com.example.EnlazadosTW.dtos.MessageResponseDto;
import com.example.EnlazadosTW.dtos.RefreshTokenRequestDto;
import com.example.EnlazadosTW.dtos.ResendVerificationEmailRequestDto;
import com.example.EnlazadosTW.dtos.ResetPasswordRequestDto;
import com.example.EnlazadosTW.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
		return ResponseEntity.ok(authenticationService.authenticate(loginRequest));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponseDto> refreshToken(
		@Valid @RequestBody RefreshTokenRequestDto refreshRequest
	) {
		return ResponseEntity.ok(authenticationService.refreshAccessToken(refreshRequest.refreshToken()));
	}

	@GetMapping("/verify-email")
	public ResponseEntity<MessageResponseDto> verifyEmail(@RequestParam String token) {
		return ResponseEntity.ok(authenticationService.verifyEmail(token));
	}

	@PostMapping("/resend-verification")
	public ResponseEntity<MessageResponseDto> resendVerificationEmail(
		@Valid @RequestBody ResendVerificationEmailRequestDto request
	) {
		return ResponseEntity.ok(authenticationService.resendVerificationEmail(request));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<MessageResponseDto> forgotPassword(
		@Valid @RequestBody ForgotPasswordRequestDto request
	) {
		return ResponseEntity.ok(authenticationService.requestPasswordReset(request));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<MessageResponseDto> resetPassword(
		@Valid @RequestBody ResetPasswordRequestDto request
	) {
		return ResponseEntity.ok(authenticationService.resetPassword(request));
	}

	@GetMapping("/me")
	public ResponseEntity<String> getCurrentUser() {
		return ResponseEntity.ok("Autenticacion valida");
	}
}
