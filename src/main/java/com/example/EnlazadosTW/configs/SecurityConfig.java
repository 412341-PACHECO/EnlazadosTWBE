package com.example.EnlazadosTW.configs;

import com.example.EnlazadosTW.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

/**
 * Configuración de Spring Security con JWT.
 * Habilita autenticación basada en tokens sin usar sesiones.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	/**
	 * Define el encoder para las contraseñas.
	 * Utiliza BCrypt que es adecuado para almacenar contraseñas de forma segura.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Expone el AuthenticationManager como bean.
	 * Spring Security 6.x configura automáticamente el proveedor DAO.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	/**
	 * Configura la cadena de filtros de seguridad HTTP.
	 * - Sin sesiones (stateless)
	 * - JWT en cada solicitud
	 * - CORS habilitado (opcional)
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authz -> authz
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// ===== SWAGGER - TODO DEBE SER PUBLICO =====
				.requestMatchers("/swagger-ui.html").permitAll()
				.requestMatchers("/swagger-ui/**").permitAll()
				.requestMatchers("/v3/api-docs").permitAll()
				.requestMatchers("/v3/api-docs/**").permitAll()
				.requestMatchers("/swagger-resources").permitAll()
				.requestMatchers("/swagger-resources/**").permitAll()
				.requestMatchers("/webjars/**").permitAll()
				
				// ===== Endpoints públicos de autenticación =====
				.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
				
				// ===== Endpoints públicos de usuarios (crear usuario) =====
				.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
				
				// ===== Endpoints públicos de roles (lectura) =====
				.requestMatchers(HttpMethod.GET, "/api/roles").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/roles/**").permitAll()
				
				// ===== Otros endpoints públicos =====
				.requestMatchers("/api/public/**").permitAll()
				
				// ===== Todos los demás endpoints requieren autenticación =====
				.anyRequest().authenticated()
			)
			.authenticationManager(authenticationManager)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}
