package com.avensys.rts.userservice.service;

import java.util.*;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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

import com.avensys.rts.userservice.api.exception.ServiceException;
import com.avensys.rts.userservice.constants.MessageConstants;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.payload.InstrospectResponseDTO;
import com.avensys.rts.userservice.payload.LoginDTO;
import com.avensys.rts.userservice.payload.LoginResponseDTO;
import com.avensys.rts.userservice.payload.LogoutResponseDTO;
import com.avensys.rts.userservice.repository.UserRepository;
import com.avensys.rts.userservice.util.JwtUtil;
import com.avensys.rts.userservice.util.KeyCloackUtil;
import com.avensys.rts.userservice.util.ResponseUtil;

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
	private KeyCloackUtil keyCloackUtil;

	@Autowired
	private JwtUtil jwtUtil;

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

	@Override
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		UserEntity user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(
				() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

		Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

//		Set<GrantedAuthority> authorities = list.map((role) -> new SimpleGrantedAuthority(role.getName()))
//				.collect(Collectors.toSet());
		return new User(user.getEmail(), user.getPassword(), authorities);
	}

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

		// add check for email exists in DB
		if (user.getEmployeeId() != null && userRepository.existsByEmployeeId(user.getEmployeeId())) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMPLOYEE_ID_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		String password = user.getPassword();
		String encodedPassword = passwordEncoder.encode(password);
		user.setPassword(encodedPassword);

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
			userRepository.save(user);
		} else {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_KEYCLOACK_USER_CREATION, null,
					LocaleContextHolder.getLocale()));
		}
	}

	public void update(UserEntity user) throws ServiceException {

		Optional<UserEntity> dbUser = userRepository.findByUsername(user.getUsername());

		// add check for username exists in a DB
		if (dbUser.isPresent() && dbUser.get().getId() != user.getId()) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		dbUser = userRepository.findByEmail(user.getEmail());

		// add check for email exists in DB
		if (dbUser.isPresent() && dbUser.get().getId() != user.getId()) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		dbUser = userRepository.findByEmployeeId(user.getEmployeeId());

		// add check for email exists in DB
		if (dbUser.isPresent() && dbUser.get().getId() != user.getId() && user.getEmployeeId() != null) {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMPLOYEE_ID_TAKEN, null,
					LocaleContextHolder.getLocale()));
		}

		UserEntity userById = getUserById(user.getId());

		if (userById.getKeycloackId() != null) {
			String password = userById.getPassword();

			if (user.getPassword() != null && user.getPassword().length() > 0) {
				String encodedPassword = passwordEncoder.encode(user.getPassword());
				password = encodedPassword;
				userById.setPassword(password);
			}

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
			usersResource.get(userById.getKeycloackId()).update(kcUser);
			usersResource.get(userById.getKeycloackId()).resetPassword(credential);
			user.setKeycloackId(userById.getKeycloackId());

			userById.setFirstName(user.getFirstName());
			userById.setLastName(user.getLastName());
			userById.setUsername(user.getUsername());
			userById.setEmail(user.getEmail());
			userById.setMobile(user.getMobile());

			if (user.getEmployeeId() != null) {
				userById.setEmployeeId(user.getEmployeeId());
			}

			userRepository.save(userById);
		} else {
			throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_PROVIDE_KEYCLOAK_ID,
					new Object[] { user.getId() }, LocaleContextHolder.getLocale()));
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
		UserEntity userEntity = userRepository.findByUsernameOrEmail(loginDTO.getUsername(), loginDTO.getUsername())
				.orElseThrow(
						() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_NOT_FOUND,
								new Object[] { loginDTO.getUsername() }, LocaleContextHolder.getLocale())));
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
		UserEntity user = userRepository.findByUsernameOrEmail(email, email)
				.orElseThrow(() -> new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_EXIST,
						null, LocaleContextHolder.getLocale())));
		return user;
	}

	public Page<UserEntity> getFormListingPage(Integer page, Integer size, String sortBy, String sortDirection) {
		System.out.println("Sorting");
		Sort sort = null;
		System.out.println("Sort By: " + sortBy);
		if (sortBy != null) {
			// Get direction based on sort direction
			Sort.Direction direction = Sort.DEFAULT_DIRECTION;
			if (sortDirection != null) {
				direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
			}
			sort = Sort.by(direction, sortBy);
		} else {
//			sort = Sort.by(Sort.Direction.DESC, "updatedAt");
			sort = Sort.by(Sort.Direction.DESC, "id");
		}
		System.out.println("Test 3");
		Pageable pageable = null;
		if (page == null && size == null) {
			pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);
		} else {
			pageable = PageRequest.of(page, size, sort);
		}
		Page<UserEntity> usersPage = userRepository.findAllByPaginationAndSort(pageable);
		return usersPage;
	}


	public Page<UserEntity> getFormListingPageWithSearch(Integer page, Integer size, String sortBy, String sortDirection, String searchTerm) {
		System.out.println("Sorting With Search");
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
		List<String> customView = List.of("id", "firstName", "lastName", "employeeId");

//		Specification<UserEntity> specification = (root, query, criteriaBuilder) -> {
//
//			List<Predicate> predicates = new ArrayList<>();
//
//			// Custom fields you want to search in
//			for (String field : customView) {
////				if ("baseFormName".equals(field)) {
////					Join<FormsEntity, FormsEntity> baseJoin = root.join("baseForm", JoinType.LEFT);
////					predicates.add(criteriaBuilder.like(criteriaBuilder.lower(baseJoin.get("formName")), "%" + searchTerm.toLowerCase() + "%"));
////				} else {
//					Path<Object> fieldPath = root.get(field);
//
//					if (fieldPath.getJavaType() == Integer.class) {
//						try {
//							Integer id = Integer.parseInt(searchTerm);
//							predicates.add(criteriaBuilder.equal(fieldPath, id));
//						} catch (NumberFormatException e) {
//							// Ignore if it's not a valid integer
//						}
//					} else {
//						predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fieldPath.as(String.class)), "%" + searchTerm.toLowerCase() + "%"));
//					}
////				}
//			}
//			Predicate searchOrPredicates = criteriaBuilder.or(predicates.toArray(new Predicate[0]));
//
//			return criteriaBuilder.and(searchOrPredicates);
//		};

		Page<UserEntity> usersPage = userRepository.findAll(getSpecification(searchTerm,customView), pageable);

		return usersPage;
	}

	private Specification<UserEntity> getSpecification(String searchTerm, List<String> customView) {
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
					predicates.add(criteriaBuilder.like(criteriaBuilder.lower(fieldPath.as(String.class)), "%" + searchTerm.toLowerCase() + "%"));
				}
//				}
			}
			Predicate searchOrPredicates = criteriaBuilder.or(predicates.toArray(new Predicate[0]));
			return criteriaBuilder.and(searchOrPredicates);
		};
	}

}