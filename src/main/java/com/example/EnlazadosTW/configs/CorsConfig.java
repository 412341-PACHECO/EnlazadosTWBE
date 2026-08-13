package com.example.EnlazadosTW.configs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	private final List<String> allowedOriginPatterns;

	public CorsConfig(
		@Value("${app.cors.allowed-origin-patterns:http://localhost:8100,http://localhost,http://127.0.0.1:8100,capacitor://localhost,https://*.pages.dev}") String allowedOriginPatterns
	) {
		this.allowedOriginPatterns = Arrays.stream(allowedOriginPatterns.split(","))
			.map(String::trim)
			.filter(StringUtils::hasText)
			.collect(Collectors.toList());
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(allowedOriginPatterns);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
