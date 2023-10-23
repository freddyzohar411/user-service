package com.avensys.rts.userservice.util;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyCloackUtil {

	private static Keycloak keycloak = null;

	@Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-id}")
	private static String clientId;

	@Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-secret}")
	private static String clientSecret;

	@Value("${keycloak.realm}")
	private static String realm;

	@Value("${keycloak.auth-server-url}")
	private static String serverUrl;

	@Value("${keycloak.master-realm}")
	private static String masterRealm;

	@Value("${keycloak.admin-client-id}")
	private static String adminClientId;

	@Value("${keycloak.admin-username}")
	private static String adminUserName;

	@Value("${keycloak.admin-password}")
	private static String adminPassword;

	private KeyCloackUtil() {
	}

	public static Keycloak getInstance() {
		if (keycloak == null) {
			keycloak = KeycloakBuilder.builder().serverUrl(serverUrl).realm(masterRealm).clientId(adminClientId)
					.clientSecret(clientSecret).username(adminUserName) // Admin username
					.password(adminPassword) // Admin password
					.build();
		}
		return keycloak;
	}

	public RealmResource getRealm() {
		Keycloak keycloak = getInstance();
		RealmResource realmResource = keycloak.realm(realm);
		return realmResource;
	}

}
