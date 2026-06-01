package com.example.EnlazadosTW.security;

import com.example.EnlazadosTW.services.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro JWT que valida el token en cada solicitud.
 * Se ejecuta una vez por solicitud HTTP.
 * Ignora rutas públicas como Swagger y endpoints de autenticación.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
								   CustomUserDetailsService userDetailsService) {
		this.tokenProvider = tokenProvider;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Valida el JWT token y establece la autenticación en el contexto de seguridad.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									 HttpServletResponse response,
									 FilterChain filterChain) throws ServletException, IOException {
		try {
			String jwt = extractJwtFromRequest(request);

			if (jwt != null && tokenProvider.validateToken(jwt)) {
				String email = tokenProvider.getEmailFromToken(jwt);
				UserDetails userDetails = userDetailsService.loadUserByUsername(email);

				// Crear autenticación manualmente (sin credenciales)
				Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
					userDetails,
					null,
					userDetails.getAuthorities()
				);

				// Establecer en el contexto de seguridad
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception ex) {
			logger.error("No se pudo validar el JWT token", ex);
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Saltarse el filtro JWT para rutas públicas.
	 * Esto permite que Spring Security maneje estas rutas sin pasar por el filtro.
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		
		// Saltarse para CUALQUIER ruta de swagger, webjars, v3
		if (path.contains("swagger") || path.contains("webjars") || path.contains("/v3/")) {
			return true;
		}
		
		// Saltarse para endpoints públicos
		if (path.startsWith("/api/auth/") ||
			(path.equals("/api/users") && request.getMethod().equals("POST")) ||
			path.startsWith("/api/roles")) {
			return true;
		}
		
		return false;
	}

	/**
	 * Extrae el token JWT del header Authorization.
	 * Espera formato: "Bearer <token>"
	 *
	 * @param request solicitud HTTP
	 * @return token JWT o null si no existe
	 */
	private String extractJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7); // Remover "Bearer "
		}

		return null;
	}

}
