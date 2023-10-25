package com.avensys.rts.userservice.util;

import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyCloackUtil {

	private static Keycloak keycloak = null;

	private static String clientSecret;
	private static String realm;
	private static String serverUrl;
	private static String masterRealm;
	private static String adminClientId;
	private static String adminUserName;
	private static String adminPassword;

	@Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-secret}")
	public void setClientSecret(String clientSecret) {
		KeyCloackUtil.clientSecret = clientSecret;
	}

	@Value("${keycloak.realm}")
	public void setRealm(String realm) {
		KeyCloackUtil.realm = realm;
	}

	@Value("${keycloak.auth-server-url}")
	public void setServerUrl(String serverUrl) {
		KeyCloackUtil.serverUrl = serverUrl;
	}

	@Value("${keycloak.master-realm}")
	public void setMasterRealm(String masterRealm) {
		KeyCloackUtil.masterRealm = masterRealm;
	}

	@Value("${keycloak.admin-client-id}")
	public void setAdminClientId(String adminClientId) {
		KeyCloackUtil.adminClientId = adminClientId;
	}

	@Value("${keycloak.admin-username}")
	public void setAdminUserName(String adminUserName) {
		KeyCloackUtil.adminUserName = adminUserName;
	}

	@Value("${keycloak.admin-password}")
	public void setAdminPassword(String adminPassword) {
		KeyCloackUtil.adminPassword = adminPassword;
	}

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

	public static CredentialRepresentation createPasswordCredentials(String password) {
		CredentialRepresentation passwordCredentials = new CredentialRepresentation();
		passwordCredentials.setTemporary(false);
		passwordCredentials.setType(CredentialRepresentation.PASSWORD);
		passwordCredentials.setValue(password);
		return passwordCredentials;
	}

	public RealmResource getRealm() {
		Keycloak keycloak = getInstance();
		RealmResource realmResource = keycloak.realm(realm);
		return realmResource;
	}

	public String getIdByEmail(String email) {
		List<UserRepresentation> users = getRealm().users().searchByEmail(email, true);
		List<String> ids = users.stream().map(userData -> userData.getId()).collect(Collectors.toList());
		if (ids.size() > 0) {
			return ids.get(0);
		} else {
			return null;
		}
	}

	public String getIdByUsername(String username) {
		List<UserRepresentation> users = getRealm().users().searchByUsername(username, true);
		List<String> ids = users.stream().map(userData -> userData.getId()).collect(Collectors.toList());
		if (ids.size() > 0) {
			return ids.get(0);
		} else {
			return null;
		}
	}

}
