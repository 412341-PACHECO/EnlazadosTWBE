package com.example.EnlazadosTW.security;

import com.example.EnlazadosTW.exceptions.InvalidTokenException;
import com.example.EnlazadosTW.exceptions.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Proveedor de JWT para generar, validar y extraer información de tokens.
 */
@Component
public class JwtTokenProvider {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expiration}")
	private long jwtExpiration;

	@Value("${app.jwt.refresh-expiration}")
	private long refreshTokenExpiration;

	/**
	 * Obtiene la clave secreta firmada.
	 */
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}

	/**
	 * Genera un JWT token a partir de UserDetails.
	 *
	 * @param userDetails datos del usuario autenticado
	 * @return token JWT
	 */
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userDetails.getUsername(), jwtExpiration);
	}

	/**
	 * Genera un token de refresco (refresh token).
	 *
	 * @param userDetails datos del usuario autenticado
	 * @return refresh token JWT
	 */
	public String generateRefreshToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
	}

	/**
	 * Crea un token JWT con los claims y expiración especificada.
	 */
	private String createToken(Map<String, Object> claims, String subject, long expiration) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder()
			.claims(claims)
			.subject(subject)
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigningKey())
			.compact();
	}

	/**
	 * Extrae el email (subject) del token JWT.
	 *
	 * @param token token JWT
	 * @return email del usuario
	 */
	public String getEmailFromToken(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extrae la fecha de expiración del token.
	 *
	 * @param token token JWT
	 * @return fecha de expiración
	 */
	public Date getExpirationDateFromToken(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Extrae un claim específico del token.
	 *
	 * @param token token JWT
	 * @param claimsResolver función para extraer el claim
	 * @param <T> tipo del claim
	 * @return valor del claim
	 */
	public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Obtiene todos los claims del token.
	 *
	 * @param token token JWT
	 * @return claims del token
	 * @throws TokenExpiredException si el token ha expirado
	 * @throws InvalidTokenException si el token es inválido
	 */
	private Claims getAllClaimsFromToken(String token) {
		try {
			return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException ex) {
			throw new TokenExpiredException("El token ha expirado", ex);
		} catch (Exception ex) {
			throw new InvalidTokenException("Token inválido o malformado", ex);
		}
	}

	/**
	 * Verifica si el token ha expirado.
	 *
	 * @param token token JWT
	 * @return true si ha expirado, false en caso contrario
	 */
	private Boolean isTokenExpired(String token) {
		try {
			Date expiration = getExpirationDateFromToken(token);
			return expiration.before(new Date());
		} catch (TokenExpiredException ex) {
			throw ex;
		} catch (Exception e) {
			throw new InvalidTokenException("No se pudo determinar la expiración del token", e);
		}
	}

	/**
	 * Valida si el token es válido para el usuario especificado.
	 *
	 * @param token token JWT
	 * @param userDetails datos del usuario
	 * @return true si el token es válido
	 * @throws TokenExpiredException si el token ha expirado
	 * @throws InvalidTokenException si el token es inválido
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String email = getEmailFromToken(token);
		return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	/**
	 * Valida si el token tiene un formato correcto y no ha expirado.
	 *
	 * @param token token JWT
	 * @return true si es válido
	 * @throws TokenExpiredException si el token ha expirado
	 * @throws InvalidTokenException si el token es inválido o malformado
	 */
	public Boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token);
			
			if (isTokenExpired(token)) {
				throw new TokenExpiredException();
			}
			return true;
		} catch (TokenExpiredException ex) {
			throw ex;
		} catch (ExpiredJwtException ex) {
			throw new TokenExpiredException("El token ha expirado", ex);
		} catch (Exception e) {
			throw new InvalidTokenException("Token inválido o malformado", e);
		}
	}

}
