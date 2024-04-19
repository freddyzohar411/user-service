package com.avensys.rts.userservice.service;

/**
 *  This is the test class for User service
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.avensys.rts.userservice.api.exception.ServiceException;
import com.avensys.rts.userservice.constants.MessageConstants;
import com.avensys.rts.userservice.entity.ForgetPasswordEntity;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.entity.UserGroupEntity;
import com.avensys.rts.userservice.payload.InstrospectResponseDTO;
import com.avensys.rts.userservice.payload.LoginDTO;
import com.avensys.rts.userservice.payload.LoginResponseDTO;
import com.avensys.rts.userservice.payload.LogoutResponseDTO;
import com.avensys.rts.userservice.payload.UserRequestDTO;
import com.avensys.rts.userservice.repository.UserRepository;
import com.avensys.rts.userservice.util.JwtUtil;
//import com.avensys.rts.userservice.util.KeyCloackUtil;
public class UserServiceTest {

	
	@Autowired
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private MessageSource messageSource;

	@Mock
	private RestTemplate restTemplate;

	Page<UserEntity> usersPage;
	Page<UserEntity> userPageAsc;;
	LoginResponseDTO loginResponseDTO;
	LogoutResponseDTO logoutResponseDTO;
	InstrospectResponseDTO instrospectResponseDTO;
	ResponseEntity<LoginResponseDTO> UserResponse;
	ResponseEntity<LogoutResponseDTO> responseLogout;
	ResponseEntity<InstrospectResponseDTO> response;
	HttpEntity<MultiValueMap<String, String>> httpEntity;

	List<UserEntity> userList;
	Specification<UserEntity> userSpecifications;
	@Autowired
	JwtUtil jwtUtil;
	Set<Long> ids;

	UserRequestDTO userRequestDTO;
	UserRequestDTO userRequestDTO1;
	Optional<UserEntity> dbUser;
	LoginDTO loginDTO;
	AutoCloseable autoCloseable;
	Set<UserEntity> users;
	List<Long> groups;
	UserEntity manager;
	Pageable pageable = null;
	List<ForgetPasswordEntity> forgetPassword = new ArrayList<>();
	Pageable pageableAsc = null;
	UserEntity userEntity;
	UserEntity userEntity1;
	Set<UserGroupEntity> groupEntities;
	Sort sortDec = null;
	Sort sortAsc = null;
	String token;

	// @Autowired
	// private KeyCloackUtil keyCloackUtil;

	/**
	 * This setup method is invoking before each test method
	 */
	@BeforeEach
	void setUp() {
		autoCloseable = MockitoAnnotations.openMocks(this);
		logoutResponseDTO = new LogoutResponseDTO("User Logout successfully");
		responseLogout = ResponseEntity.status(HttpStatus.OK).body(logoutResponseDTO);
		instrospectResponseDTO = new InstrospectResponseDTO(true);
		response = ResponseEntity.status(HttpStatus.OK).body(instrospectResponseDTO);
		userService = new UserService(userRepository);
		// restTemplate = new RestTemplate();
		userEntity = new UserEntity(1L, "339f35a7-0d3d-431e-9a63-d90d4c342e4a", "Kotaiah", "Nalleb",
				"kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", false, true,
				groupEntities, users, manager,true,forgetPassword,"india","AP","Developer");
		userEntity1 = new UserEntity(2L, "339f35a7-0d3d-431e-9a63-d90d4c342e4a", "Kittu", "Nallebeboina",
				"kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", false, true,
				groupEntities, users, manager,true,forgetPassword,"india","AP","Developer");
		userRequestDTO = new UserRequestDTO(1L, "Kotaiah", "Nalleb", "kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", 1L,"AP","india","developer",groups,true);
		userRequestDTO1 = new UserRequestDTO(2L, "Kotaiah", "Nalleb", "kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", 1L,"AP","india","developer",groups,true);
		userRepository.save(userEntity);
		dbUser = Optional.of(userEntity);
		userList = Arrays.asList(userEntity, userEntity1);
		sortDec = Sort.by(Sort.Direction.DESC, "updatedAt");
		sortAsc = Sort.by(Sort.Direction.ASC, "updatedAt");
		pageableAsc = PageRequest.of(1, 2, sortAsc);
		pageable = PageRequest.of(1, 2, sortDec);
		userPageAsc = new PageImpl<UserEntity>(userList, pageableAsc, 2);
		usersPage = new PageImpl<UserEntity>(userList, pageable, 2);
		loginDTO = new LoginDTO("kittu1@aven-sys.com", "pass1234");
		ids = new HashSet<Long>();
		ids.add(2L);
		ids.add(3L);
		users = new HashSet<UserEntity>();
		users.add(userEntity);
		users.add(userEntity1);
		token = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5rpmWoMY4HaqclmHY9QgpU/S3+/kWc1fcAPbTiCyUW7PywPCAWb9gWDIkHRs1BtCmIbhhhHfQed83IkS6cTDrexSpNTqnD2YlgNyqk5aUaD0UpimU1Dw3Xhly7W5+Pbi9qOd6LW4GsAPamIQvnYUAWb9xaGX5nfZA54V72cX1Bw6kdF0Z+WxiNjMnG24XhxSM7b/3fW4gYkoBIu/uCx/b9if/GANgqasjISlf1SNQA95cGPwDycbRUv3MCXx0eFLpkDRsL+j6lKTSCQWWb1Aq8gjsXQRrNToI7HvobwBpu8G9XKHlqYmNG0bRT/pZxbFJQQ5t4Hbba14eDnQm/5SFQIDAQAB";
	}

	@AfterEach
	void tearDown() throws Exception {
		autoCloseable.close();
	}

	@Test
	void testSaveUserPositive() throws Exception {
		mock(ServiceException.class);
		mock(UserRequestDTO.class);
		mock(UserRepository.class);
		mock(MessageConstants.class);
		mock(UserEntity.class);
		mock(RealmResource.class);
		mock(UsersResource.class);
		mock(UserRepresentation.class);
		mock(MessageSource.class);
		mock(CredentialRepresentation.class);
		// userService.saveUser(userRequestDTO, 1L);
		Boolean existsByEmail = false;
		Boolean existsByUsername = false;
		Boolean existsByEmployeeId = false;
		when(userRepository.existsByUsername(userRequestDTO.getUsername())).thenReturn(existsByUsername);
		when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(existsByEmail);
		when(userRepository.existsByEmployeeId(userRequestDTO.getEmployeeId())).thenReturn(existsByEmployeeId);
		when(userRepository.findById(userRequestDTO.getManagerId())).thenReturn(dbUser);
		when(userRepository.save(userEntity)).thenReturn(userEntity);
		assertThat(userRepository.save(userEntity).getFirstName()).isEqualTo("Kotaiah");

	}

	@Test
	void testSaveUserNegative() throws Exception {
		mock(ServiceException.class);
		mock(UserRequestDTO.class);
		mock(UserRepository.class);
		mock(MessageConstants.class);
		mock(UserEntity.class);
		mock(RealmResource.class);
		mock(UsersResource.class);
		mock(UserRepresentation.class);
		mock(MessageSource.class);
		mock(CredentialRepresentation.class);
		Boolean existsByEmailUser = false;
		Boolean existsByEmail = true;
		when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(existsByEmail);
		try {
			if (existsByEmail) {
				throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USERNAME_TAKEN, null,
						LocaleContextHolder.getLocale()));
			}

		} catch (Exception e) {
			existsByEmailUser = true;
		}
		assertTrue(existsByEmailUser);

	}

	@Test
	void testUpdatePositive() throws Exception {
		mock(ServiceException.class);
		mock(UserRequestDTO.class);
		mock(MessageSource.class);
		mock(ServiceException.class);
		when(userRepository.findByUsername(userRequestDTO.getUsername())).thenReturn(dbUser);
		when(userRepository.findByEmail(userRequestDTO.getEmail())).thenReturn(dbUser);
		when(userRepository.findByEmployeeId(userRequestDTO.getEmployeeId())).thenReturn(dbUser);
		// when(userService.getUserById(userRequestDTO.getId())).thenReturn(userEntity);
		when(userRepository.findById(userRequestDTO.getManagerId())).thenReturn(dbUser);
		when(userRepository.save(userEntity)).thenReturn(userEntity);
		assertThat(userRepository.save(userEntity).getEmail()).isEqualTo("kittu1@aven-sys.com");
	}

	@Test
	void testUpdateNegative() {
		mock(ServiceException.class);
		mock(UserRequestDTO.class);
		mock(MessageSource.class);
		when(userRepository.findByEmail(userRequestDTO.getEmail())).thenReturn(dbUser);
		boolean userEmail = false;
		try {
			if (dbUser.isPresent() && dbUser.get().getId() != userRequestDTO1.getId()) {
				throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null,
						LocaleContextHolder.getLocale()));
			}
		} catch (Exception e) {
			userEmail = true;
		}
		assertTrue(userEmail);
	}

	@Test
	void testGetUserByIdPositive() {
		mock(UserRequestDTO.class);
		mock(MessageSource.class);
		mock(UserEntity.class);
		when(userRepository.findById(1L)).thenReturn(dbUser);
		assertThat(userEntity.getId()).isEqualTo(1L);
		when(userRepository.findById(userEntity.getId())).thenReturn(Optional.ofNullable(userEntity));
	}

	@Test
	void testGetUserByIdNegative() {
		mock(UserRequestDTO.class);
		mock(MessageSource.class);
		mock(UserEntity.class);
		when(userRepository.findById(1L)).thenReturn(dbUser);
		assertThat(userEntity.getId()).isEqualTo(1L);
		when(userRepository.findById(userEntity.getId())).thenReturn(Optional.ofNullable(userEntity));
		userEntity.setIsDeleted(true);
		boolean userValue = false;
		try {
			if (dbUser.isPresent() && !dbUser.get().getIsDeleted()) {

			} else {
				long id = 1;
				throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
						new Object[] { id }, LocaleContextHolder.getLocale()));
			}

		} catch (Exception e) {
			userValue = true;
		}
		assertTrue(userValue);
	}

	@Test
	void testGetUserByEmail() {
		when(userRepository.findByEmail("kittu1@aven-sys.com")).thenReturn(dbUser);
		assertThat(dbUser.get().getEmail()).isEqualTo("kittu1@aven-sys.com");
	}

	@Test
	void testFetchList() {
		when(userRepository.findAllAndIsDeleted(false)).thenReturn(userList);
		assertThat(userList.get(1).getFirstName()).isEqualTo("Kittu");
	}

	@Test
	void testLogin() {
		mock(HttpHeaders.class);
		when(restTemplate.postForEntity("http://localhost:8080/realms/rtsrealm/protocol/openid-connect/token",
				httpEntity, LoginResponseDTO.class)).thenReturn(UserResponse);
		when(userRepository.findByUsernameOrEmail(loginDTO.getUsername(), loginDTO.getUsername())).thenReturn(dbUser);
		assertNotNull(dbUser);

	}

	@Test
	void testLogout() {
		when(restTemplate.postForEntity("http://localhost:8080/realms/rtsrealm/protocol/openid-connect/logout", httpEntity,
				LogoutResponseDTO.class)).thenReturn(responseLogout);
		assertNotNull(responseLogout);
	}

	@Test
	void testGetAllUsersUnderManagerQuery()throws Exception {
		when(JwtUtil.getEmailFromContext()).thenReturn("kittu1@aven-sys.com");
		when(userRepository.findByUsernameOrEmail("Kotaiah", "kittu1@aven-sys.com")).thenReturn(dbUser);
		assertNotNull(dbUser);
		//when(userService.getAllUsersUnderManager()).thenReturn(users);
		//when(userRepository.findUserIdsUnderManager(userEntity.getId())).thenReturn(ids);
	}

	@Test
	void testGetAllUsersUnderManager(){
		when(JwtUtil.getEmailFromContext()).thenReturn("kittu1@aven-sys.com");
		when(userRepository.findByUsernameOrEmail("kittu1@aven-sys.com", "kittu1@aven-sys.com")).thenReturn(dbUser);
		when(userRepository.findUserIdsUnderManager(userEntity.getId())).thenReturn(ids);
		assertNotNull(ids);
	}

	@Test
	void testGetUserListingPageWithSearch() {
		mock(Pageable.class);
		mock(UserEntity.class);
		mock(UserRepository.class);
		when(userRepository.findAll(userSpecifications, pageable)).thenReturn(usersPage);
		when(userService.getUserListingPageWithSearch(1, 1, "updatedAt", "DEFAULT_DIRECTION", "name"))
				.thenReturn(usersPage);
		assertNotNull(usersPage);

	}

	@Test
	void testGetUserListingPage() {
		mock(Pageable.class);
		mock(UserRequestDTO.class);
		mock(MessageSource.class);
		mock(UserEntity.class);
		when(userRepository.findAllByPaginationAndSort(false, true, pageable)).thenReturn(usersPage);
		when(userService.getUserListingPage(1, 2, "updatedAt", "DEFAULT_DIRECTION","filterType")).thenReturn(usersPage);
		assertNotNull(usersPage);
	}

	@Test
	void testGetUserDetail() {
		when(JwtUtil.getEmailFromContext()).thenReturn("kittu1@aven-sys.com");
		when(userRepository.findByUsernameOrEmail("kittu1@aven-sys.com", "kittu1@aven-sys.com")).thenReturn(dbUser);
		assertNotNull(dbUser);
	}

	@Test 
	void testValidate() { 
		when(restTemplate.postForEntity("http://localhost:8080/realms/rtsrealm/protocol/openid-connect/token/introspect",httpEntity, InstrospectResponseDTO.class)).thenReturn(response);
		assertNotNull(response);
	  
	}

	@Test
	void testDelete() throws Exception {
		mock(ServiceException.class);
		mock(UserRequestDTO.class);
		mock(MessageSource.class);
		mock(UserEntity.class);
		userEntity.setIsDeleted(true);
		when(userRepository.save(userEntity)).thenReturn(userEntity);
		assertThat(userRepository.save(userEntity).getIsDeleted()).isEqualTo(true);
	}

}
