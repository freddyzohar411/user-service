package com.avensys.rts.userservice.service;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.avensys.rts.userservice.api.exception.ServiceException;
import com.avensys.rts.userservice.constants.MessageConstants;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.payload.InstrospectResponseDTO;
import com.avensys.rts.userservice.payload.LoginDTO;
import com.avensys.rts.userservice.payload.LoginResponseDTO;
import com.avensys.rts.userservice.payload.LogoutResponseDTO;
import com.avensys.rts.userservice.repository.UserRepository;
import com.avensys.rts.userservice.util.KeyCloackUtil;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private KeyCloackUtil keyCloackUtil;

	@Autowired
	private MessageSource messageSource;

	@Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
	private String tokenUrl;

	@Value("${spring.security.oauth2.client.provider.keycloak.end-session-uri}")
	private String endSessionUrl;

	@Value("${spring.security.oauth2.client.provider.keycloak.instrospect-uri}")
	private String instrospectUrl;

	@Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-secret}")
	private String clientSecret;

	@Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.authorization-grant-type}")
	private String grantType;

	public void saveUser(UserEntity user) throws ServiceException {

		// add check for username exists in a DB
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		// add check for email exists in DB
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		String password = user.getPassword();
		String encodedPassword = passwordEncoder.encode(password);
		user.setPassword(encodedPassword);
		userRepository.save(user);

		RealmResource realmResource = keyCloackUtil.getRealm();
		UsersResource usersResource = realmResource.users();

		UserRepresentation newUser = new UserRepresentation();
		newUser.setUsername(user.getUsername());
		newUser.setFirstName(user.getFirstName());
		newUser.setLastName(user.getLastName());
		newUser.setEmail(user.getEmail());
		newUser.setEmailVerified(true);
		newUser.setEnabled(true);

		// Set the user's password
		CredentialRepresentation passwordCred = new CredentialRepresentation();
		passwordCred.setType(CredentialRepresentation.PASSWORD);
		passwordCred.setValue(password); // Set the desired password
		passwordCred.setTemporary(false); // Set to false for a permanent password

		newUser.setCredentials(Arrays.asList(passwordCred));
		usersResource.create(newUser);
	}

	@Override
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		UserEntity user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(
				() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

		Set<GrantedAuthority> authorities = user.getRoles().stream()
				.map((role) -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet());

		return new User(user.getEmail(), user.getPassword(), authorities);
	}

	public LoginResponseDTO login(LoginDTO loginDTO) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", clientId);
		map.add("client_secret", clientSecret);
		map.add("grant_type", grantType);
		map.add("username", loginDTO.getUsername());
		map.add("password", loginDTO.getPassword());

		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

		ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(tokenUrl, httpEntity,
				LoginResponseDTO.class);
		return response.getBody();
	}

	public LogoutResponseDTO logout(String refreshToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", clientId);
		map.add("client_secret", clientSecret);
		map.add("refresh_token", refreshToken);

		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

		ResponseEntity<LogoutResponseDTO> response = restTemplate.postForEntity(endSessionUrl, httpEntity,
				LogoutResponseDTO.class);
		LogoutResponseDTO res = new LogoutResponseDTO();
		if (response.getStatusCode().is2xxSuccessful()) {
			res.setMessage("Logged out successfully");
		}

		return res;
	}

	public InstrospectResponseDTO validate(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", clientId);
		map.add("client_secret", clientSecret);
		map.add("token", token);

		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
		ResponseEntity<InstrospectResponseDTO> response = restTemplate.postForEntity(instrospectUrl, httpEntity,
				InstrospectResponseDTO.class);
		return response.getBody();
	}

	public void update(UserEntity user) {
		userRepository.save(user);
	}

	public Optional<UserEntity> getUserById(Long id) {
		return userRepository.findById(id);
	}

}