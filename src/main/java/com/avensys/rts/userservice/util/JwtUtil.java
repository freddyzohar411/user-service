package com.avensys.rts.userservice.util;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class JwtUtil {

	// Get from application properties
	@Value("${spring.keycloak.public-key}")
	private String publicKey;

	/**
	 * Get public key instance from public key string
	 * 
	 * @return
	 */
	private PublicKey getPublicKeyInstance() {
		try {
			byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			return keyFactory.generatePublic(publicKeySpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Logic to extract claims from token for signed key
	 * 
	 * @param token
	 * @return claims
	 */
	// Extract all claims
	public Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getPublicKeyInstance()).build().parseClaimsJws(token).getBody();
	}

	/**
	 * Logic to extract claims from token for unsigned key
	 * 
	 * @param jwtToken
	 * @return
	 */
	public Claims extractClaims(String jwtToken) {
		Claims claims = Jwts.parser().parseClaimsJwt(jwtToken).getBody();
		return claims;
	}

	/**
	 * Logic to validate signed token based on public key instance (RS256)
	 * 
	 * @param token
	 */
	// Validate the token
	public void validateToken(String token) {
		Jwts.parserBuilder().setSigningKey(getPublicKeyInstance()).build().parseClaimsJws(token);
	}

	/**
	 * Extract roles from claims
	 * 
	 * @param claims
	 * @return list of roles
	 */
	public List<String> extractRoles(Claims claims) {
		Map<String, Object> resourceAccess = claims.get("resource_access", Map.class);
		List<String> allRoles = new ArrayList<>();
		if (resourceAccess != null) {
			Map<String, Object> account = (Map<String, Object>) resourceAccess.get("account");

			if (account != null) {
				List<String> resourceRoles = (List<String>) account.get("roles");

				if (resourceRoles != null) {
					allRoles.addAll(resourceRoles);
				}
			}
		}
		return allRoles;
	}

	/**
	 * Logic for JWT Auth filtering for Authorization verification
	 * 
	 * @return email
	 */
	public static String getEmailFromContext() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			return (String) requestAttributes.getAttribute("email", RequestAttributes.SCOPE_REQUEST);
		}
		return null;
	}

	/**
	 * Logic for JWT Auth filtering for Authorization verification
	 * 
	 * @return roles
	 */
	public static List<String> getRolesFromContext() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			return (List<String>) requestAttributes.getAttribute("roles", RequestAttributes.SCOPE_REQUEST);
		}
		return null;
	}

	/**
	 * Logic to get token from the context
	 * 
	 * @return token
	 */
	public static String getTokenFromContext() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			return (String) requestAttributes.getAttribute("token", RequestAttributes.SCOPE_REQUEST);
		}
		return null;
	}
}
