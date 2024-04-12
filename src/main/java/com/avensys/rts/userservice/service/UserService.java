package com.avensys.rts.userservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.avensys.rts.userservice.entity.OTPEnity;
import com.avensys.rts.userservice.payload.*;
import com.avensys.rts.userservice.repository.OTPRepository;
import com.avensys.rts.userservice.util.*;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.avensys.rts.userservice.APIClient.EmailAPIClient;
import com.avensys.rts.userservice.api.exception.PasswordMismatchException;
import com.avensys.rts.userservice.api.exception.ServiceException;
import com.avensys.rts.userservice.api.exception.TokenInvalidException;
import com.avensys.rts.userservice.constants.MessageConstants;
import com.avensys.rts.userservice.entity.ForgetPasswordEntity;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.entity.UserGroupEntity;
import com.avensys.rts.userservice.repository.ForgetPasswordRepository;
import com.avensys.rts.userservice.repository.UserGroupRepository;
import com.avensys.rts.userservice.repository.UserRepository;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.ws.rs.core.Response;

@Transactional
@Service
public class UserService implements UserDetailsService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserGroupRepository userGroupRepository;

	@Autowired
	private ForgetPasswordRepository forgetPasswordRepository;

	@Autowired
	private OTPRepository otpRepository;

	@Autowired
	private EmailAPIClient emailAPIClient;

	@Autowired
	private KeyCloackUtil keyCloackUtil;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private JwtUtil jwtUtil;

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

	@Value("${api.application.url}")
	private String applicationUrl;

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final String EMAIL_TEMPLATE = "Email Templates";

	@Override
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		UserEntity user = userRepository.findByUsernameOrEmailIgnoreCase(usernameOrEmail, usernameOrEmail).orElseThrow(
				() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

		Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

		return new User(user.getEmail(), user.getPassword(), authorities);
	}

	/**
	 * Save user (Modified by HX)
	 * 
	 * @param userRequest
	 * @param createdByUserId
	 * @throws ServiceException
	 */
	@Transactional
	public void saveUser(UserRequestDTO userRequest, Long createdByUserId) throws ServiceException {

		// add check for username exists in a DB
		if (userRepository.existsByUsername(userRequest.getUsername())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		// add check for email exists in DB
		if (userRepository.existsByEmail(userRequest.getEmail())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		// add check for email exists in DB
		if (userRequest.getEmployeeId() != null && userRepository.existsByEmployeeId(userRequest.getEmployeeId())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMPLOYEE_ID_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		// Create a new user entity
		UserEntity user = new UserEntity();

		String password = userRequest.getPassword();
		String encodedPassword = passwordEncoder.encode(password);
		user.setPassword(encodedPassword);

		// set fields, as this is new user, active = true and deleted = false
		user.setIsActive(Boolean.TRUE);
		user.setIsDeleted(Boolean.FALSE);

		// Set created by and updated by
		if (createdByUserId != null) {
			user.setCreatedBy(createdByUserId);
			user.setUpdatedBy(createdByUserId);
		}

		// Set user fields
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setMobile(userRequest.getMobile());
		user.setUsername(userRequest.getUsername());
		user.setEmployeeId(userRequest.getEmployeeId());
		user.setEmail(userRequest.getEmail());
		user.setIsTemp(true);

		// Added by Hx 11122023 - Add Manager
		if (userRequest.getManagerId() != null) {
			UserEntity manager = userRepository.findById(userRequest.getManagerId()).orElseThrow(
					() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
							new Object[] { user.getId() }, LocaleContextHolder.getLocale())));
			user.setManager(manager);
		}

		// Added by Hx 28022024
		user.setCountry(userRequest.getCountry());
		user.setLocation(userRequest.getLocation());
		user.setDesignation(userRequest.getDesignation());

		// Added by Hx 29022024
		user.setIsActive(userRequest.getStatus());

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
		CredentialRepresentation passwordCred = KeyCloackUtil.createPasswordCredentials(password);

		newUser.setCredentials(Collections.singletonList(passwordCred));
		Response response = usersResource.create(newUser);
		String kcId = CreatedResponseUtil.getCreatedId(response);
		if (kcId != null) {
			// Save to the database
			user.setKeycloackId(kcId);
			UserEntity savedUser = userRepository.save(user);

			// Added by Rahul Sahu to send email
			try {
				EmailMultiTemplateRequestDTO emailMultiTemplateRequestDTO = new EmailMultiTemplateRequestDTO();
				emailMultiTemplateRequestDTO.setTo(new String[] { savedUser.getEmail() });
				emailMultiTemplateRequestDTO.setSubject("Welcome to Avensys!");
				emailMultiTemplateRequestDTO.setTemplateName("User Registration");
				emailMultiTemplateRequestDTO.setCategory(EMAIL_TEMPLATE);

				Map<String, String> templateMap = new HashMap<>();
				templateMap.put("user.firstName", savedUser.getFirstName());
				templateMap.put("user.lastName", savedUser.getLastName());

				emailMultiTemplateRequestDTO.setTemplateMap(templateMap);
				emailAPIClient.sendEmailServiceTemplate(emailMultiTemplateRequestDTO);
			} catch (Exception e) {
				log.error("Error:", e);
			}

			if (!userRequest.getGroups().isEmpty()) {
				UserAddUserGroupsRequestDTO userAddUserGroupsRequestDTO = new UserAddUserGroupsRequestDTO();
				userAddUserGroupsRequestDTO.setUserId(savedUser.getId());
				userAddUserGroupsRequestDTO.setUserGroupIds(userRequest.getGroups());
				addUserGroups(userAddUserGroupsRequestDTO, savedUser);
			}

		} else {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_KEYCLOACK_USER_CREATION, null,
					LocaleContextHolder.getLocale()));
		}
	}

	@Transactional
	public void saveUsers(List<UserRequestDTO> userRequests, Long createdByUserId) throws ServiceException {
		List<UserEntity> usersToSave = new ArrayList<>();
		for (UserRequestDTO userRequest : userRequests) {
			System.out.println("Creating user for: " + userRequest.getFirstName());
			UserEntity user = createUserFromRequest(userRequest, createdByUserId);
			usersToSave.add(user);

		}
	}

	public UserEntity createUserFromRequest(UserRequestDTO userRequest, Long createdByUserId) throws ServiceException {
		// add check for username exists in a DB
		if (userRepository.existsByUsername(userRequest.getUsername())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		// add check for email exists in DB
		if (userRepository.existsByEmail(userRequest.getEmail())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		// add check for email exists in DB
		if (userRequest.getEmployeeId() != null && userRepository.existsByEmployeeId(userRequest.getEmployeeId())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMPLOYEE_ID_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		UserEntity user = new UserEntity();
		// Set Password
		String password = "password1234";
		String encodedPassword = passwordEncoder.encode(password);
		user.setPassword(encodedPassword);
		// Set Status
		user.setIsDeleted(Boolean.FALSE);
		user.setIsActive(Boolean.TRUE);
		// Set Created and Updated By
		if (createdByUserId != null) {
			user.setCreatedBy(createdByUserId);
			user.setUpdatedBy(createdByUserId);
		}

		// Set User Fields
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setUsername(userRequest.getUsername());
		user.setEmail(userRequest.getEmail());
		user.setMobile(userRequest.getMobile());
		user.setEmployeeId(userRequest.getEmployeeId());
		user.setIsTemp(true);
		// Set Other User Fields
		user.setCountry(null);
		user.setDesignation(null);
		user.setLocation(null);
		// Set Manager
		if (userRequest.getManagerId() != null) {
			UserEntity manager = userRepository.findById(userRequest.getManagerId()).orElseThrow(
					() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
							new Object[] { user.getId() }, LocaleContextHolder.getLocale())));
			user.setManager(manager);
		}

		try {
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
			CredentialRepresentation passwordCred = KeyCloackUtil.createPasswordCredentials(password);
			newUser.setCredentials(Collections.singletonList(passwordCred));
			Response response = usersResource.create(newUser);
			String kcId = CreatedResponseUtil.getCreatedId(response);
			if (kcId != null) {
				user.setKeycloackId(kcId);
				UserEntity savedUser = userRepository.save(user);
				if (!userRequest.getGroups().isEmpty()) {
					UserAddUserGroupsRequestDTO userAddUserGroupsRequestDTO = new UserAddUserGroupsRequestDTO();
					userAddUserGroupsRequestDTO.setUserId(savedUser.getId());
					userAddUserGroupsRequestDTO.setUserGroupIds(userRequest.getGroups());
					addUserGroups(userAddUserGroupsRequestDTO, savedUser);
				} else {
					// Log the error and continue
					System.err.println("No groups specified for user: " + userRequest.getUsername());
				}
			} else {
				// Log the error and continue
				System.err.println("Keycloak ID not generated for user: " + userRequest.getUsername());
			}
		} catch (Exception e) {
			// Log the error and continue
			System.err.println("Error creating user: " + e.getMessage());
			e.printStackTrace(); // Print stack trace for debugging
		}
		return null;
	}

	public void loginResetPassword(ResetLoginRequestDTO resetLoginRequestDTO) throws ServiceException {

		resetLoginRequestDTO.setPassword(PasswordUtil.decode(resetLoginRequestDTO.getPassword()));
		resetLoginRequestDTO.setConfirmPassword(PasswordUtil.decode(resetLoginRequestDTO.getConfirmPassword()));

		// add check for username exists in a DB
		Optional<UserEntity> userOptional = userRepository.findById(resetLoginRequestDTO.getUserId());
		if (userOptional.isEmpty()) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
					new Object[] { resetLoginRequestDTO.getUserId() }, LocaleContextHolder.getLocale()));
		}

		UserEntity user = userOptional.get();

		String password = resetLoginRequestDTO.getPassword();
		String encodedPassword = passwordEncoder.encode(password);

		if (passwordEncoder.matches(password, user.getPassword())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_OLD_PASSWORD, null,
					LocaleContextHolder.getLocale()));
		}

		user.setPassword(encodedPassword);

		// set fields, as this is new user, active = true and deleted = false
		user.setIsActive(Boolean.TRUE);
		user.setIsDeleted(Boolean.FALSE);
		user.setIsTemp(false);
		user.setUpdatedBy(resetLoginRequestDTO.getUserId());

		if (user.getKeycloackId() != null) {
			CredentialRepresentation credential = KeyCloackUtil.createPasswordCredentials(password);
			UserRepresentation kcUser = new UserRepresentation();
			kcUser.setUsername(user.getUsername());
			kcUser.setFirstName(user.getFirstName());
			kcUser.setLastName(user.getLastName());
			kcUser.setEmail(user.getEmail());
			kcUser.setEmailVerified(true);
			kcUser.setEnabled(true);
			kcUser.setCredentials(Collections.singletonList(credential));

			UsersResource usersResource = keyCloackUtil.getRealm().users();
			usersResource.get(user.getKeycloackId()).update(kcUser);
			usersResource.get(user.getKeycloackId()).resetPassword(credential);
			UserEntity savedUser = userRepository.save(user);

			// Added by Rahul Sahu to send email
			try {
				EmailMultiTemplateRequestDTO emailMultiTemplateRequestDTO = new EmailMultiTemplateRequestDTO();
				emailMultiTemplateRequestDTO.setTo(new String[] { savedUser.getEmail() });
				emailMultiTemplateRequestDTO.setSubject("Congratulations! Your password has been reset successfully.");
				emailMultiTemplateRequestDTO.setTemplateName("User Password Reset");
				emailMultiTemplateRequestDTO.setCategory(EMAIL_TEMPLATE);

				Map<String, String> templateMap = new HashMap<>();
				templateMap.put("user.firstName", savedUser.getFirstName());
				templateMap.put("user.lastName", savedUser.getLastName());

				emailMultiTemplateRequestDTO.setTemplateMap(templateMap);
				emailAPIClient.sendEmailServiceTemplate(emailMultiTemplateRequestDTO);
			} catch (Exception e) {
				log.error("Error:", e);
			}

		} else {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_PROVIDE_KEYCLOAK_ID,
					new Object[] { user.getId() }, LocaleContextHolder.getLocale()));
		}

	}

	// Updated by Hx 11122023 - Update Manager and fix password update in db and
	// keycloak
	public void update(UserRequestDTO userRequest, Long createdByUserId) throws ServiceException {

		Optional<UserEntity> dbUser = userRepository.findByUsername(userRequest.getUsername());

		// add check for username exists in a DB
		if (dbUser.isPresent() && dbUser.get().getId() != userRequest.getId()) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		dbUser = userRepository.findByEmail(userRequest.getEmail());

		// add check for email exists in DB
		if (dbUser.isPresent() && dbUser.get().getId() != userRequest.getId()) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		dbUser = userRepository.findByEmployeeId(userRequest.getEmployeeId());

		// add check for email exists in DB
		if (dbUser.isPresent() && dbUser.get().getId() != userRequest.getId() && userRequest.getEmployeeId() != null) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMPLOYEE_ID_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		UserEntity userById = getUserById(userRequest.getId());

		// Added by Hx 11122023 - Update Manager
		if (userRequest.getManagerId() != null) {
			UserEntity manager = userRepository.findById(userRequest.getManagerId()).orElseThrow(
					() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
							new Object[] { userRequest.getId() }, LocaleContextHolder.getLocale())));
			userById.setManager(manager);
		} else {
			userById.setManager(null);
		}

		if (userById.getKeycloackId() != null) {
			String password = userRequest.getPassword();

			if (userRequest.getPassword() != null && userRequest.getPassword().length() > 0) {
				String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
				userById.setPassword(encodedPassword);
			}

			CredentialRepresentation credential = KeyCloackUtil.createPasswordCredentials(password);
			UserRepresentation kcUser = new UserRepresentation();
			kcUser.setUsername(userRequest.getUsername());
			kcUser.setFirstName(userRequest.getFirstName());
			kcUser.setLastName(userRequest.getLastName());
			kcUser.setEmail(userRequest.getEmail());
			kcUser.setEmailVerified(true);
			kcUser.setEnabled(true);
			kcUser.setCredentials(Collections.singletonList(credential));

			UsersResource usersResource = keyCloackUtil.getRealm().users();
			usersResource.get(userById.getKeycloackId()).update(kcUser);
			usersResource.get(userById.getKeycloackId()).resetPassword(credential);

			userById.setFirstName(userRequest.getFirstName());
			userById.setLastName(userRequest.getLastName());
			userById.setUsername(userRequest.getUsername());
			userById.setEmail(userRequest.getEmail());
			userById.setMobile(userRequest.getMobile());
			userById.setUpdatedBy(createdByUserId);

			// Added by Hx 29022024
			userById.setLocation(userRequest.getLocation());
			userById.setCountry(userRequest.getCountry());
			userById.setDesignation(userRequest.getDesignation());

			// Added by Hx 29022024
			userById.setIsActive(userRequest.getStatus());

			if (userRequest.getEmployeeId() != null) {
				userById.setEmployeeId(userRequest.getEmployeeId());
			}

			UserEntity savedUser = userRepository.save(userById);

			// Added by Rahul Sahu to send email
			try {
				EmailMultiTemplateRequestDTO emailMultiTemplateRequestDTO = new EmailMultiTemplateRequestDTO();
				emailMultiTemplateRequestDTO.setTo(new String[] { savedUser.getEmail() });
				emailMultiTemplateRequestDTO.setSubject("Congratulations! Your profile has been updated successfully.");
				emailMultiTemplateRequestDTO.setTemplateName("User Profile Update");
				emailMultiTemplateRequestDTO.setCategory(EMAIL_TEMPLATE);

				Map<String, String> templateMap = new HashMap<>();
				templateMap.put("user.firstName", savedUser.getFirstName());
				templateMap.put("user.lastName", savedUser.getLastName());

				emailMultiTemplateRequestDTO.setTemplateMap(templateMap);
				emailAPIClient.sendEmailServiceTemplate(emailMultiTemplateRequestDTO);
			} catch (Exception e) {
				log.error("Error:", e);
			}

			if (!userRequest.getGroups().isEmpty()) {
				List<UserGroupEntity> groups = userGroupRepository.findAll();
				if (groups.size() > 0) {
					groups.forEach(grp -> {
						if (grp.getUsers().contains(userById)) {
							grp.getUsers().remove(userById);
						}
					});
				}
				UserAddUserGroupsRequestDTO userAddUserGroupsRequestDTO = new UserAddUserGroupsRequestDTO();
				userAddUserGroupsRequestDTO.setUserId(savedUser.getId());
				userAddUserGroupsRequestDTO.setUserGroupIds(userRequest.getGroups());
				addUserGroups(userAddUserGroupsRequestDTO, savedUser);
			}

		} else {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_PROVIDE_KEYCLOAK_ID,
					new Object[] { userRequest.getId() }, LocaleContextHolder.getLocale()));
		}
	}

	public void delete(Long id) throws ServiceException {
		UserEntity dbUser = getUserById(id);
		if (dbUser.getKeycloackId() != null) {
			UsersResource usersResource = keyCloackUtil.getRealm().users();
			usersResource.get(dbUser.getKeycloackId()).remove();

			dbUser.setIsDeleted(true);
			userRepository.save(dbUser);
		} else {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
					new Object[] { id }, LocaleContextHolder.getLocale()));
		}
	}

	public UserEntity getUserById(Long id) throws ServiceException {
		if (id == null) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_PROVIDE_ID, new Object[] { id },
					LocaleContextHolder.getLocale()));
		}

		Optional<UserEntity> user = userRepository.findById(id);
		if (user.isPresent() && !user.get().getIsDeleted()) {
			return user.get();
		} else {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
					new Object[] { id }, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Get user by username
	 *
	 * @param email
	 * @return
	 */
	public UserEntity getUserByEmail(String email) throws ServiceException {
		UserEntity user = userRepository.findByEmail(email).orElseThrow(
				() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_NOT_FOUND,
						new Object[] { email }, LocaleContextHolder.getLocale())));
		return user;
	}

	public List<UserEntity> fetchList() {
		return (List<UserEntity>) userRepository.findAllAndIsDeleted(false);
	}

	public LoginResponseDTO login(LoginDTO loginDTO) throws ServiceException {
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

		LoginResponseDTO res = response.getBody();
		// Get userEnitity from repository
		UserEntity userEntity = userRepository
				.findByUsernameOrEmailIgnoreCase(loginDTO.getUsername(), loginDTO.getUsername()).orElseThrow(
						() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_NOT_FOUND,
								new Object[] { loginDTO.getUsername() }, LocaleContextHolder.getLocale())));
		res.setUser(ResponseUtil.mapUserEntitytoResponse(userEntity));
		return res;
	}

	public LoginResponseDTO login1FA(LoginDTO loginDTO) throws ServiceException {
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

		LoginResponseDTO res = response.getBody();
		// Get userEnitity from repository
		UserEntity userEntity = userRepository
				.findByUsernameOrEmailIgnoreCase(loginDTO.getUsername(), loginDTO.getUsername()).orElseThrow(
						() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_NOT_FOUND,
								new Object[] { loginDTO.getUsername() }, LocaleContextHolder.getLocale())));
		res.setUser(ResponseUtil.mapUserEntitytoResponse(userEntity));

		// Create a OTP
		OTPEnity otp = createOTP(userEntity, 6, 5);

		// Send email with template
		sendOTPEmail(userEntity, otp);

		return res;
	}

	public LoginResponseDTO login2FA(OTPRequestDTO otpRequestDTO) throws ServiceException {
		UserEntity user = getUserDetail();

		// Get OTP
		OTPEnity otp = otpRepository.findByUserAndOTPToken(user, otpRequestDTO.getOtp())
				.orElseThrow(() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_OTP_NOTFOUND,
						null, LocaleContextHolder.getLocale())));

		if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_OTP_EXPIRED, null,
					LocaleContextHolder.getLocale()));
		}

		if (otp.isUsed()) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_OTP_INVALID, null,
					LocaleContextHolder.getLocale()));
		}

		otp.setUsed(true);
		otpRepository.save(otp);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", clientId);
		map.add("client_secret", clientSecret);
		map.add("grant_type", "refresh_token");
		map.add("refresh_token", otpRequestDTO.getRefreshToken());

		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

		ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(tokenUrl, httpEntity,
				LoginResponseDTO.class);

		LoginResponseDTO res = response.getBody();

		res.setUser(ResponseUtil.mapUserEntitytoResponse(user));
		return res;
	}

	private OTPEnity createOTP(UserEntity user, int otpLength, int expiryTimeInMinutes) {
		OTPEnity otp = new OTPEnity();
		otp.setOtpToken(OTPUtil.generateNumericOtp(otpLength));
		otp.setExpiryTime(LocalDateTime.now().plusMinutes(expiryTimeInMinutes));
		otp.setUser(user);
		otp.setUsed(false);
		return otpRepository.save(otp);
	}

	private void sendOTPEmail(UserEntity user, OTPEnity otp) {
		EmailMultiTemplateRequestDTO emailMultiTemplateRequestDTO = new EmailMultiTemplateRequestDTO();
		emailMultiTemplateRequestDTO.setTo(new String[] { user.getEmail() });
		emailMultiTemplateRequestDTO.setSubject("OTP for 2FA");
		emailMultiTemplateRequestDTO.setTemplateName("Login OTP Template");
		emailMultiTemplateRequestDTO.setCategory("Email Templates");
		emailMultiTemplateRequestDTO.setSubCategory("Login OTP");
		Map<String, String> templateMap = new HashMap<>();
		templateMap.put("LOGIN_OTP_TOKEN", otp.getOtpToken());
		emailMultiTemplateRequestDTO.setTemplateMap(templateMap);
		emailMultiTemplateRequestDTO.setContent("OTP for 2FA is " + otp.getOtpToken());
		emailAPIClient.sendEmailServiceTemplate(emailMultiTemplateRequestDTO);
	}

	public void resendOTP() throws ServiceException {
		UserEntity user = getUserDetail();
		OTPEnity otp = createOTP(user, 6, 5);
		sendOTPEmail(user, otp);
	}

	public LoginResponseDTO refreshToken(RefreshTokenDTO refreshTokenDTO) throws ServiceException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", clientId);
		map.add("client_secret", clientSecret);
		map.add("grant_type", "refresh_token");
		map.add("refresh_token", refreshTokenDTO.getRefreshToken());

		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

		ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(tokenUrl, httpEntity,
				LoginResponseDTO.class);

		LoginResponseDTO res = response.getBody();
		// Get userEnitity from repository
		UserEntity userEntity = userRepository.findById(refreshTokenDTO.getId())
				.orElseThrow(() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
						new Object[] { refreshTokenDTO.getId() }, LocaleContextHolder.getLocale())));
		res.setUser(ResponseUtil.mapUserEntitytoResponse(userEntity));
		return res;
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
			res.setMessage(messageSource.getMessage(MessageConstants.USER_LOGOUT_SUCCESS, null,
					LocaleContextHolder.getLocale()));
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

	public UserEntity getUserDetail() throws ServiceException {
		String email = JwtUtil.getEmailFromContext();
		UserEntity user = userRepository.findByUsernameOrEmailIgnoreCase(email, email)
				.orElseThrow(() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_EXIST,
						null, LocaleContextHolder.getLocale())));
		return user;
	}

	public Page<UserEntity> getUserListingPage(Integer page, Integer size, String sortBy, String sortDirection,
			String filterType) {
		Sort sort = null;
		if (sortBy != null) {
			// Get direction based on sort direction
			Sort.Direction direction = Sort.DEFAULT_DIRECTION;
			if (sortDirection != null) {
				direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
			}
			sort = Sort.by(direction, sortBy);
		} else {
			sort = Sort.by(Sort.Direction.DESC, "updatedAt");
		}
		System.out.println("Test 3");
		Pageable pageable = null;
		if (page == null && size == null) {
			pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);
		} else {
			pageable = PageRequest.of(page, size, sort);
		}
		// new
		if (filterType.equalsIgnoreCase("deleted")) {
			Page<UserEntity> usersPage = userRepository.findAllByIsInDeletedPaginationAndSort(true, pageable);
			return usersPage;
		} else if (filterType.equalsIgnoreCase("inactive")) {
			Page<UserEntity> usersPage = userRepository.findAllByPaginationAndSort(false, false, pageable);
			return usersPage;
		} else {
			Page<UserEntity> usersPage = userRepository.findAllByPaginationAndSort(false, true, pageable);

			return usersPage;
		}
	}

	public Page<UserEntity> getUserListingPageWithSearch(Integer page, Integer size, String sortBy,
			String sortDirection, String searchTerm) {
		Sort sort = null;
		if (sortBy != null) {
			// Get direction based on sort direction
			Sort.Direction direction = Sort.DEFAULT_DIRECTION;
			if (sortDirection != null) {
				direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
			}
			sort = Sort.by(direction, sortBy);
		} else {
			sort = Sort.by(Sort.Direction.DESC, "updatedAt");
		}

		Pageable pageable = null;
		if (page == null && size == null) {
			pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);
		} else {
			pageable = PageRequest.of(page, size, sort);
		}

		// Dynamic search based on custom view (future feature)
		List<String> customView = List.of("lastName", "firstName", "employeeId", "createdAt");

		Page<UserEntity> usersPage = userRepository.findAll(getSpecification(searchTerm, customView, false, true),
				pageable);

		return usersPage;
	}

	private Specification<UserEntity> getSpecification(String searchTerm, List<String> customView, Boolean isDeleted,
			Boolean isActive) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			// Custom fields you want to search in
			for (String field : customView) {
				Path<Object> fieldPath = root.get(field);
				if (fieldPath.getJavaType() == Integer.class) {
					try {
						Integer id = Integer.parseInt(searchTerm);
						predicates.add(criteriaBuilder.equal(fieldPath, id));
					} catch (NumberFormatException e) {
						// Ignore if it's not a valid integer
					}
				} else {
					predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fieldPath.as(String.class)),
							"%" + searchTerm.toLowerCase() + "%"));
				}
			}

			Predicate searchOrPredicates = criteriaBuilder.or(predicates.toArray(new Predicate[0]));

			List<Predicate> fixPredicates = new ArrayList<>();
			// Add conditions for isDeleted and isActive
			fixPredicates.add(criteriaBuilder.equal(root.get("isDeleted"), isDeleted));
			fixPredicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));

			// Combine all predicates with AND
			Predicate finalPredicate = criteriaBuilder.and(searchOrPredicates,
					criteriaBuilder.and(fixPredicates.toArray(new Predicate[0])));

			return finalPredicate;
		};
	}

	/**
	 * Get all users under a manager (In Java)
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public Set<UserEntity> getAllUsersUnderManager() throws ServiceException {
		Set<UserEntity> allUsersUnderManager = new HashSet<>();
		String email = JwtUtil.getEmailFromContext();
		UserEntity manager = userRepository.findByUsernameOrEmailIgnoreCase(email, email)
				.orElseThrow(() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_EXIST,
						null, LocaleContextHolder.getLocale())));
		if (manager != null) {
			recursivelyGetUsersUnderManager(manager, allUsersUnderManager);
		}
		return allUsersUnderManager;
	}

	private void recursivelyGetUsersUnderManager(UserEntity manager, Set<UserEntity> result) {
		if (manager != null) {
			result.add(manager);
			// Recursively get users under each subordinate manager
			if (manager.getUsers() != null) {
				for (UserEntity subordinate : manager.getUsers()) {
					recursivelyGetUsersUnderManager(subordinate, result);
				}
			}
		}
	}

	/**
	 * Get all users under a manager (In SQL)
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public Set<Long> getAllUsersUnderManagerQuery() throws ServiceException {
		String email = JwtUtil.getEmailFromContext();
		UserEntity manager = userRepository.findByUsernameOrEmailIgnoreCase(email, email)
				.orElseThrow(() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_EXIST,
						null, LocaleContextHolder.getLocale())));
		return userRepository.findUserIdsUnderManager(manager.getId());
	}

	/**
	 * Get all users under a manager (In SQL) - Entity
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public Set<UserEntity> getAllUsersEntityUnderManagerQuery() throws ServiceException {
		String email = JwtUtil.getEmailFromContext();
		UserEntity manager = userRepository.findByUsernameOrEmailIgnoreCase(email, email)
				.orElseThrow(() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_EXIST,
						null, LocaleContextHolder.getLocale())));
		return userRepository.findUserEntitiesUnderManager(manager.getId());
	}

	/**
	 * Forget Password (Send a reset email to user if email exist)
	 * 
	 * @param email
	 * @return
	 * @throws ServiceException
	 */
	@Transactional
	public String forgetPassword(String email) throws ServiceException {
		UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new ServiceException(messageSource
				.getMessage(MessageConstants.ERROR_USER_NOT_EXIST, null, LocaleContextHolder.getLocale())));

		// Generate a token
		UUID uuid = UUID.randomUUID();
		String token = uuid.toString();

		// Save a forget Entity first
		ForgetPasswordEntity forgetPassword = new ForgetPasswordEntity();
		forgetPassword.setToken(token);
		forgetPassword.setUser(user);
		forgetPassword.setExpiryTime(LocalDateTime.now().plusHours(24));

		forgetPasswordRepository.save(forgetPassword);

		// Send email with template
		EmailMultiTemplateRequestDTO emailMultiTemplateRequestDTO = new EmailMultiTemplateRequestDTO();
		emailMultiTemplateRequestDTO.setTo(new String[] { email });
		emailMultiTemplateRequestDTO.setSubject("Reset Password");
		emailMultiTemplateRequestDTO.setTemplateName("Reset Template 1");
		emailMultiTemplateRequestDTO.setCategory("Email Templates");
		emailMultiTemplateRequestDTO.setSubCategory("Reset Password");
		Map<String, String> templateMap = new HashMap<>();
		templateMap.put("RESET_PASSWORD_LINK", applicationUrl + "/forget-reset-password?token=" + token);
		emailMultiTemplateRequestDTO.setTemplateMap(templateMap);
		emailMultiTemplateRequestDTO.setContent("Please click the link to reset your password: " + applicationUrl
				+ "/forget-reset-password?token=" + token);
		emailAPIClient.sendEmailServiceTemplate(emailMultiTemplateRequestDTO);

		return token;
	}

	/**
	 * Validate forget password token
	 * 
	 * @param token
	 * @return
	 */
	public Boolean validateForgetPasswordToken(String token) {
		Optional<ForgetPasswordEntity> forgetPasswordEntity = forgetPasswordRepository.findByToken(token);
		if (forgetPasswordEntity.isPresent() && !forgetPasswordEntity.get().isUsed()
				&& forgetPasswordEntity.get().getExpiryTime().isAfter(LocalDateTime.now())) {
			return true;
		}
		return false;
	}

	/**
	 * Forget password reset
	 * 
	 * @param forgetResetPasswordRequestDTO
	 * @throws ServiceException
	 */
	@Transactional
	public void forgetPasswordReset(ForgetResetPasswordRequestDTO forgetResetPasswordRequestDTO)
			throws ServiceException, TokenInvalidException, PasswordMismatchException {
		// Check token is valid
		Optional<ForgetPasswordEntity> forgetPasswordEntity = forgetPasswordRepository
				.findByToken(forgetResetPasswordRequestDTO.getToken());
		if (!forgetPasswordEntity.isPresent() || forgetPasswordEntity.get().isUsed() == true
				|| forgetPasswordEntity.get().getExpiryTime().isBefore(LocalDateTime.now())) {
			throw new TokenInvalidException("Token is invalid");
		}

		// Check password and confirm password are the same
		String password = PasswordUtil.decode(forgetResetPasswordRequestDTO.getPassword());
		String confirmPassword = PasswordUtil.decode(forgetResetPasswordRequestDTO.getConfirmPassword());
		if (!password.equals(confirmPassword)) {
			throw new PasswordMismatchException("Password and confirm password do not match");
		}

		// Get User from token
		UserEntity user = forgetPasswordEntity.get().getUser();

		// Check if no user throw exception
		if (user == null) {
			throw new ServiceException("User not found");
		}

		String encodedPassword = passwordEncoder.encode(password);

		user.setPassword(encodedPassword);

		// set fields, as this is new user, active = true and deleted = false
		user.setIsActive(Boolean.TRUE);
		user.setIsDeleted(Boolean.FALSE);
		user.setUpdatedBy(user.getId());

		if (user.getKeycloackId() != null) {
			CredentialRepresentation credential = KeyCloackUtil.createPasswordCredentials(password);
			UserRepresentation kcUser = new UserRepresentation();
			kcUser.setUsername(user.getUsername());
			kcUser.setFirstName(user.getFirstName());
			kcUser.setLastName(user.getLastName());
			kcUser.setEmail(user.getEmail());
			kcUser.setEmailVerified(true);
			kcUser.setEnabled(true);
			kcUser.setCredentials(Collections.singletonList(credential));

			UsersResource usersResource = keyCloackUtil.getRealm().users();
			usersResource.get(user.getKeycloackId()).update(kcUser);
			usersResource.get(user.getKeycloackId()).resetPassword(credential);
			userRepository.save(user);

			// Set forget password entity to used
			forgetPasswordEntity.get().setUsed(true);
			forgetPasswordRepository.save(forgetPasswordEntity.get());
			sendConfirmationEmail(user.getEmail());
		} else {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_PROVIDE_KEYCLOAK_ID,
					new Object[] { user.getId() }, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Send confirmation email helper
	 * 
	 * @param email
	 */
	private void sendConfirmationEmail(String email) {
		EmailMultiTemplateRequestDTO emailMultiTemplateRequestDTO = new EmailMultiTemplateRequestDTO();
		emailMultiTemplateRequestDTO.setTo(new String[] { email });
		emailMultiTemplateRequestDTO.setSubject("Confirm Password Reset");
		emailMultiTemplateRequestDTO.setTemplateName("Confirm Reset");
		emailMultiTemplateRequestDTO.setCategory("Email Templates");
		emailMultiTemplateRequestDTO.setSubCategory("Confirm Password Reset");
		emailMultiTemplateRequestDTO.setContent("Your password has been reset successfully");
		emailAPIClient.sendEmailServiceTemplate(emailMultiTemplateRequestDTO);
	}

	public void addUserGroups(UserAddUserGroupsRequestDTO userAddUserGroupsRequestDTO, UserEntity savedUser)
			throws ServiceException {
		Long updateUserId = getUserId();
		if (savedUser != null) {
			List<Long> userGroups = userAddUserGroupsRequestDTO.getUserGroupIds();
			userGroups.forEach(id -> {
				Optional<UserGroupEntity> userGroupEntity = userGroupRepository.findById(id);
				if (userGroupEntity.isPresent()) {
					userGroupEntity.get().addUser(savedUser);
					userGroupEntity.get().setUpdatedBy(updateUserId);
					userGroupRepository.save(userGroupEntity.get());
				}
			});
		}
	}

	private Long getUserId() {
		String token = JwtUtil.getTokenFromContext();
		return jwtUtil.getUserId(token);
	}

}