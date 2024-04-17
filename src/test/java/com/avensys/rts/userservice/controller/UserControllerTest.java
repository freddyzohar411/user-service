package com.avensys.rts.userservice.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.avensys.rts.userservice.api.exception.ServiceException;
import com.avensys.rts.userservice.constants.MessageConstants;
import com.avensys.rts.userservice.entity.BaseEntity;
import com.avensys.rts.userservice.entity.ModuleEntity;
import com.avensys.rts.userservice.entity.RoleEntity;
import com.avensys.rts.userservice.entity.RoleModulePermissionsEntity;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.entity.UserGroupEntity;
import com.avensys.rts.userservice.payload.InstrospectResponseDTO;
import com.avensys.rts.userservice.payload.LoginDTO;
import com.avensys.rts.userservice.payload.LoginResponseDTO;
import com.avensys.rts.userservice.payload.LogoutResponseDTO;
import com.avensys.rts.userservice.payload.UserRequestDTO;
import com.avensys.rts.userservice.payload.response.ModuleResponseDTO;
import com.avensys.rts.userservice.payload.response.RoleResponseDTO;
import com.avensys.rts.userservice.payload.response.UserGroupResponseDTO;
import com.avensys.rts.userservice.payload.response.UserResponseDTO;
import com.avensys.rts.userservice.repository.PermissionRepository;
import com.avensys.rts.userservice.service.UserService;
import com.avensys.rts.userservice.util.JwtUtil;
import com.avensys.rts.userservice.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * This is the test class for User controller
 */
public class UserControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	UserController userController;

	@Mock
	private UserService userService;

	@Autowired
	private static PermissionRepository permissionRepository;

	@Mock
	private AuthenticationManager authenticationManager;

	@Autowired
	private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

	@Mock
	private MessageSource messageSource;

	UserEntity userEntity;
	UserEntity userEntity1;
	ModuleEntity moduleEntity;
	LoginDTO loginDTO;
	UserEntity manager;
	BaseEntity baseEntity;
	RoleEntity roleEntity;
	RoleEntity roleEntity1;
	List<UserEntity> usersList;
	LoginResponseDTO loginResponseDTO;
	UserResponseDTO userResponseDTO;
	UserGroupResponseDTO userGroupResponseDTO;
	UserGroupResponseDTO userGroupResponseDTO1;
	List<UserGroupResponseDTO> userGroup;
	RoleResponseDTO roleResponseDTO;
	List<RoleResponseDTO> roles;
	ModuleResponseDTO moduleResponseDTO;
	ModuleResponseDTO moduleResponseDTO1;
	List<ModuleResponseDTO> modules1;
	List<String> permissionsList1;
	Set<UserGroupEntity> groupEntities;
	UserGroupEntity userGroupEntity;
	RoleModulePermissionsEntity roleModulePermissionsEntity;
	RoleModulePermissionsEntity roleModulePermissionsEntity1;
	Set<RoleModulePermissionsEntity> modulePermissions;
	UserRequestDTO userRequestDTO;
	UserRequestDTO userRequestDTO1;
	LocalDateTime localeDate;
	String str = "2023-12-07 10:29:40.421602";
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	UserResponseDTO Manager;
	InstrospectResponseDTO instrospectResponseDTO;
	LogoutResponseDTO logoutResponseDTO;
	@Mock
	Authentication authenticate;

	Set<UserEntity> users;

	Set<RoleEntity> roleEntities;

	@MockBean
	AutoCloseable autoCloseable;

	Optional<UserEntity> user;

	@Autowired
	ServiceException serviceException;

	@Autowired
	MessageConstants messageConstants;

	@Mock
	private JwtUtil jwtUtil;
	Date date = new Date();

	/**
	 * This setup method is invoking before each test method
	 */
	@BeforeEach
	void setUp() {
		autoCloseable = MockitoAnnotations.openMocks(this);
		logoutResponseDTO = new LogoutResponseDTO();
		logoutResponseDTO.setMessage("Logout successfull");
		instrospectResponseDTO = new InstrospectResponseDTO();
		instrospectResponseDTO.setActive(true);
		userRequestDTO = new UserRequestDTO(1L, "Kotai", "Nalleb", "kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", 1L);
		userRequestDTO1 = new UserRequestDTO(1L, "Kotai", "Nalleb", "kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", 1L);
		moduleEntity = new ModuleEntity(1L, "Accounts", "Accounts module .", modulePermissions);
		roleModulePermissionsEntity = new RoleModulePermissionsEntity(1L, moduleEntity, roleEntity, "permissions");
		roleModulePermissionsEntity1 = new RoleModulePermissionsEntity(2L, moduleEntity, roleEntity, "permissions");
		modulePermissions = new HashSet<RoleModulePermissionsEntity>();
		modulePermissions.add(roleModulePermissionsEntity);
		modulePermissions.add(roleModulePermissionsEntity1);
		roleEntity = new RoleEntity(1L, "Super Admin", "Super admin access to all modules!", modulePermissions,
				groupEntities);
		roleEntity1 = new RoleEntity(2L, "Super Admin", "Super admin access to all modules!", modulePermissions,
				groupEntities);
		userGroupEntity = new UserGroupEntity(1L, "sales", "sales Group description", users, roleEntities);
		userEntity = new UserEntity(1L, "339f35a7-0d3d-431e-9a63-d90d4c342e4a", "Kotai", "Nalleb",
				"kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", false, true,
				groupEntities, users, manager);
		userEntity1 = new UserEntity(2L, "339f35a7-0d3d-431e-9a63-d90d4c342e4a", "Kotaiah", "Nalleb",
				"kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", false, true,
				groupEntities, users, manager);
		user = Optional.of(userEntity);
		permissionsList1 = Arrays.asList("Read", "Write", "Edit", "Delete");
		moduleResponseDTO = new ModuleResponseDTO(1L, "Hi", permissionsList1);
		moduleResponseDTO1 = new ModuleResponseDTO(1L, "Hi1", permissionsList1);
		modules1 = Arrays.asList(moduleResponseDTO, moduleResponseDTO1);
		usersList = Arrays.asList(userEntity, userEntity1);
		roleResponseDTO = new RoleResponseDTO(1L, "Super Admin", "Super admin access to all modules!", modules1);
		userGroupResponseDTO = new UserGroupResponseDTO(1L, "sales", "sales Group description", roles);
		userGroupResponseDTO1 = new UserGroupResponseDTO(2L, "recruiter", "recruiter description", roles);
		userGroup = Arrays.asList(userGroupResponseDTO, userGroupResponseDTO1);
		// localeDate = LocalDateTime.parse(str, formatter);
		userResponseDTO = new UserResponseDTO(1L, "339f35a7-0d3d-431e-9a63-d90d4c342e4a", "Kotai", "Nalleb",
				"kittu1@aven-sys.com", "kittu1@aven-sys.com", "9381515362", "234", false, true, userGroup, 1L,
				localeDate, Manager);
		loginDTO = new LoginDTO("kittu1@aven-sys.com", "pass1234");
		loginResponseDTO = new LoginResponseDTO();
		usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(),
				loginDTO.getPassword());
		// authenticate.setAuthenticated(true);
		// ResponseUtil.setPermissionRepository(permissionRepository);
		serviceException = new ServiceException(messageSource.getMessage(messageConstants.ERROR_USER_NOT_FOUND,
				new Object[] { 1 }, LocaleContextHolder.getLocale()));
		this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
	}

	/**
	 * This tearDown method is used to cleanup the object initialization and other
	 * resources.
	 * 
	 * @throws Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
		autoCloseable.close();

	}

	@Test
	void testAuthenticateUserPositive() throws Exception {
		assertNotNull(usernamePasswordAuthenticationToken);
		when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenReturn(authenticate);
		when(authenticate.isAuthenticated()).thenReturn(true);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/signin")
				.content(asJsonString(new LoginDTO("kittu1@aven-sys.com", "pass1234")))
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isOk());
	}

	@Test
	void testAuthenticateUserNegative() throws Exception {
		assertNotNull(usernamePasswordAuthenticationToken);
		when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenReturn(authenticate);
		when(authenticate.isAuthenticated()).thenReturn(false);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/signin")
				.content(asJsonString(new LoginDTO("kittu1@aven-sys.com", "pass1234")))
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isUnauthorized());
	}

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	void testRegisterUserPositive() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/signup")
				.content(asJsonString(new UserRequestDTO(1L, "Kotai", "Nalleb", "kittu1@aven-sys.com",
						"kittu1@aven-sys.com", "$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO",
						"9381515362", "234", 1L)))
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isCreated());
	}

	@Test
	void testRegisterUserPositive1() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		UserRequestDTO userRequestDTO = new UserRequestDTO(1L, "Kotai", "Nalleb", "kittu1@aven-sys.com",
				"kittu1@aven-sys.com", "$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362",
				"234", 1L);
		assertNotNull(userRequestDTO.getUsername());
		UserRequestDTO userRequestDTO2 = new UserRequestDTO();
		userRequestDTO2.setLastName("nalleboina");
		userRequestDTO2.setEmployeeId("123");
		userRequestDTO2.setPassword("$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO");
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/signup").content(asJsonString(userRequestDTO2))
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isCreated());
	}

	@Test
	void testRegisterUserNegative() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/signup")
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isBadRequest());
	}

	@Test
	void testValidate() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0WndUaGhXVUtGSjhUdE1NdFZrcm1Edk9TdGdRcS1Sa3MwUnEwRE5IRG5jIn0.eyJleHAiOjE3MDMyMzI3MTQsImlhdCI6MTcwMzIzMjQxNCwianRpIjoiNmMwYjBlMmYtMDZmYi00YzU3LWJmMWQtM2MzNmEzZGUxOGQxIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9ydHNyZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIzMzlmMzVhNy0wZDNkLTQzMWUtOWE2My1kOTBkNGMzNDJlNGEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJydHNjbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL3d3dy5rZXljbG9hay5vcmciXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLXJ0c3JlYWxtIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJLb3RhaSBOYWxsZWIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJraXR0dTFAYXZlbi1zeXMuY29tIiwiZ2l2ZW5fbmFtZSI6IktvdGFpIiwiZmFtaWx5X25hbWUiOiJOYWxsZWIiLCJlbWFpbCI6ImtpdHR1MUBhdmVuLXN5cy5jb20ifQ.A314CP_nu6x3qENsK8fyZP8SXXJO9y1nAcUXHU2FRRZ2vtPjD-T6rUoHQ_CZgMXnPg4Rl4MOlSCQ5leTiWix9kfBYkDQGar7GPSf9UnnPai7adiLV8Rb6OUYykHPjN_Wy3A0CVyGbsBB1ow7uhmgPkM7aMBUUYikkYK0aLremKn9vXJCpC7G2UTCW_BOjl7Bb5atic3J328ieN8nu0_W_Zd61ux1zm7skX4TPLNTC-4dAc16O-6IOo6JChQLUublfm-CcVC_i7oIv0Nuw7hOj5m5_e0klNcK-dw9bArBkRCGU9Sr4ieFIkjaLxt22Z3ZDg0C9SeB268OvnKXrjDKiQ";
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/validate").header("Authorization",
				"Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0WndUaGhXVUtGSjhUdE1NdFZrcm1Edk9TdGdRcS1Sa3MwUnEwRE5IRG5jIn0.eyJleHAiOjE3MDMyMzI3MTQsImlhdCI6MTcwMzIzMjQxNCwianRpIjoiNmMwYjBlMmYtMDZmYi00YzU3LWJmMWQtM2MzNmEzZGUxOGQxIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9ydHNyZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIzMzlmMzVhNy0wZDNkLTQzMWUtOWE2My1kOTBkNGMzNDJlNGEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJydHNjbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL3d3dy5rZXljbG9hay5vcmciXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLXJ0c3JlYWxtIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJLb3RhaSBOYWxsZWIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJraXR0dTFAYXZlbi1zeXMuY29tIiwiZ2l2ZW5fbmFtZSI6IktvdGFpIiwiZmFtaWx5X25hbWUiOiJOYWxsZWIiLCJlbWFpbCI6ImtpdHR1MUBhdmVuLXN5cy5jb20ifQ.A314CP_nu6x3qENsK8fyZP8SXXJO9y1nAcUXHU2FRRZ2vtPjD-T6rUoHQ_CZgMXnPg4Rl4MOlSCQ5leTiWix9kfBYkDQGar7GPSf9UnnPai7adiLV8Rb6OUYykHPjN_Wy3A0CVyGbsBB1ow7uhmgPkM7aMBUUYikkYK0aLremKn9vXJCpC7G2UTCW_BOjl7Bb5atic3J328ieN8nu0_W_Zd61ux1zm7skX4TPLNTC-4dAc16O-6IOo6JChQLUublfm-CcVC_i7oIv0Nuw7hOj5m5_e0klNcK-dw9bArBkRCGU9Sr4ieFIkjaLxt22Z3ZDg0C9SeB268OvnKXrjDKiQ")
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		when(userService.validate(token)).thenReturn(instrospectResponseDTO);
		assertNotNull(instrospectResponseDTO);
		assertTrue(instrospectResponseDTO.getActive());
		mockMvc.perform(request).equals(instrospectResponseDTO.getActive());
	}

	@Test
	void testLogout() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0WndUaGhXVUtGSjhUdE1NdFZrcm1Edk9TdGdRcS1Sa3MwUnEwRE5IRG5jIn0.eyJleHAiOjE3MDMyMzI3MTQsImlhdCI6MTcwMzIzMjQxNCwianRpIjoiNmMwYjBlMmYtMDZmYi00YzU3LWJmMWQtM2MzNmEzZGUxOGQxIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9ydHNyZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIzMzlmMzVhNy0wZDNkLTQzMWUtOWE2My1kOTBkNGMzNDJlNGEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJydHNjbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL3d3dy5rZXljbG9hay5vcmciXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLXJ0c3JlYWxtIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJLb3RhaSBOYWxsZWIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJraXR0dTFAYXZlbi1zeXMuY29tIiwiZ2l2ZW5fbmFtZSI6IktvdGFpIiwiZmFtaWx5X25hbWUiOiJOYWxsZWIiLCJlbWFpbCI6ImtpdHR1MUBhdmVuLXN5cy5jb20ifQ.A314CP_nu6x3qENsK8fyZP8SXXJO9y1nAcUXHU2FRRZ2vtPjD-T6rUoHQ_CZgMXnPg4Rl4MOlSCQ5leTiWix9kfBYkDQGar7GPSf9UnnPai7adiLV8Rb6OUYykHPjN_Wy3A0CVyGbsBB1ow7uhmgPkM7aMBUUYikkYK0aLremKn9vXJCpC7G2UTCW_BOjl7Bb5atic3J328ieN8nu0_W_Zd61ux1zm7skX4TPLNTC-4dAc16O-6IOo6JChQLUublfm-CcVC_i7oIv0Nuw7hOj5m5_e0klNcK-dw9bArBkRCGU9Sr4ieFIkjaLxt22Z3ZDg0C9SeB268OvnKXrjDKiQ";
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/logout").header("Authorization",
				"Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0WndUaGhXVUtGSjhUdE1NdFZrcm1Edk9TdGdRcS1Sa3MwUnEwRE5IRG5jIn0.eyJleHAiOjE3MDMyMzI3MTQsImlhdCI6MTcwMzIzMjQxNCwianRpIjoiNmMwYjBlMmYtMDZmYi00YzU3LWJmMWQtM2MzNmEzZGUxOGQxIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9ydHNyZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIzMzlmMzVhNy0wZDNkLTQzMWUtOWE2My1kOTBkNGMzNDJlNGEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJydHNjbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL3d3dy5rZXljbG9hay5vcmciXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLXJ0c3JlYWxtIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJLb3RhaSBOYWxsZWIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJraXR0dTFAYXZlbi1zeXMuY29tIiwiZ2l2ZW5fbmFtZSI6IktvdGFpIiwiZmFtaWx5X25hbWUiOiJOYWxsZWIiLCJlbWFpbCI6ImtpdHR1MUBhdmVuLXN5cy5jb20ifQ.A314CP_nu6x3qENsK8fyZP8SXXJO9y1nAcUXHU2FRRZ2vtPjD-T6rUoHQ_CZgMXnPg4Rl4MOlSCQ5leTiWix9kfBYkDQGar7GPSf9UnnPai7adiLV8Rb6OUYykHPjN_Wy3A0CVyGbsBB1ow7uhmgPkM7aMBUUYikkYK0aLremKn9vXJCpC7G2UTCW_BOjl7Bb5atic3J328ieN8nu0_W_Zd61ux1zm7skX4TPLNTC-4dAc16O-6IOo6JChQLUublfm-CcVC_i7oIv0Nuw7hOj5m5_e0klNcK-dw9bArBkRCGU9Sr4ieFIkjaLxt22Z3ZDg0C9SeB268OvnKXrjDKiQ")
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		when(userService.logout(token)).thenReturn(logoutResponseDTO);
		assertNotNull(logoutResponseDTO);
		mockMvc.perform(request).equals(logoutResponseDTO.getMessage());
	}

	@Test
	void testCreateUserPositive() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/add")
				.content(asJsonString(new UserRequestDTO(1L, "Kotai", "Nalleb", "kittu1@aven-sys.com",
						"kittu1@aven-sys.com", "$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO",
						"9381515362", "234", 1L)))
				.header("Authorization",
						"Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0WndUaGhXVUtGSjhUdE1NdFZrcm1Edk9TdGdRcS1Sa3MwUnEwRE5IRG5jIn0.eyJleHAiOjE3MDMyMzI3MTQsImlhdCI6MTcwMzIzMjQxNCwianRpIjoiNmMwYjBlMmYtMDZmYi00YzU3LWJmMWQtM2MzNmEzZGUxOGQxIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9ydHNyZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIzMzlmMzVhNy0wZDNkLTQzMWUtOWE2My1kOTBkNGMzNDJlNGEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJydHNjbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL3d3dy5rZXljbG9hay5vcmciXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLXJ0c3JlYWxtIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJLb3RhaSBOYWxsZWIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJraXR0dTFAYXZlbi1zeXMuY29tIiwiZ2l2ZW5fbmFtZSI6IktvdGFpIiwiZmFtaWx5X25hbWUiOiJOYWxsZWIiLCJlbWFpbCI6ImtpdHR1MUBhdmVuLXN5cy5jb20ifQ.A314CP_nu6x3qENsK8fyZP8SXXJO9y1nAcUXHU2FRRZ2vtPjD-T6rUoHQ_CZgMXnPg4Rl4MOlSCQ5leTiWix9kfBYkDQGar7GPSf9UnnPai7adiLV8Rb6OUYykHPjN_Wy3A0CVyGbsBB1ow7uhmgPkM7aMBUUYikkYK0aLremKn9vXJCpC7G2UTCW_BOjl7Bb5atic3J328ieN8nu0_W_Zd61ux1zm7skX4TPLNTC-4dAc16O-6IOo6JChQLUublfm-CcVC_i7oIv0Nuw7hOj5m5_e0klNcK-dw9bArBkRCGU9Sr4ieFIkjaLxt22Z3ZDg0C9SeB268OvnKXrjDKiQ")
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isCreated());
	}

	@Test
	void testCreateUserNegative() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		RequestBuilder request = MockMvcRequestBuilders.post("/api/user/add")
				.content(asJsonString(new UserRequestDTO(1L, "Kotai", "Nalleb", "kittu1@aven-sys.com",
						"kittu1@aven-sys.com", "$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO",
						"9381515362", "234", 1L)))
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isBadRequest());
	}

	@Test
	void testEditUserPositive() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = writer.writeValueAsString(userRequestDTO);
		RequestBuilder request = MockMvcRequestBuilders.put("/api/user/edit").content(requestJson).header(
				"Authorization",
				"Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0WndUaGhXVUtGSjhUdE1NdFZrcm1Edk9TdGdRcS1Sa3MwUnEwRE5IRG5jIn0.eyJleHAiOjE3MDMyMzI3MTQsImlhdCI6MTcwMzIzMjQxNCwianRpIjoiNmMwYjBlMmYtMDZmYi00YzU3LWJmMWQtM2MzNmEzZGUxOGQxIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9ydHNyZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIzMzlmMzVhNy0wZDNkLTQzMWUtOWE2My1kOTBkNGMzNDJlNGEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJydHNjbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL3d3dy5rZXljbG9hay5vcmciXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLXJ0c3JlYWxtIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiMzExMDI3MDYtYmJmZS00MGJjLWE4YmMtMDEzYTgzYzIzMTVlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJLb3RhaSBOYWxsZWIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJraXR0dTFAYXZlbi1zeXMuY29tIiwiZ2l2ZW5fbmFtZSI6IktvdGFpIiwiZmFtaWx5X25hbWUiOiJOYWxsZWIiLCJlbWFpbCI6ImtpdHR1MUBhdmVuLXN5cy5jb20ifQ.A314CP_nu6x3qENsK8fyZP8SXXJO9y1nAcUXHU2FRRZ2vtPjD-T6rUoHQ_CZgMXnPg4Rl4MOlSCQ5leTiWix9kfBYkDQGar7GPSf9UnnPai7adiLV8Rb6OUYykHPjN_Wy3A0CVyGbsBB1ow7uhmgPkM7aMBUUYikkYK0aLremKn9vXJCpC7G2UTCW_BOjl7Bb5atic3J328ieN8nu0_W_Zd61ux1zm7skX4TPLNTC-4dAc16O-6IOo6JChQLUublfm-CcVC_i7oIv0Nuw7hOj5m5_e0klNcK-dw9bArBkRCGU9Sr4ieFIkjaLxt22Z3ZDg0C9SeB268OvnKXrjDKiQ")
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testEditUserNegative() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = writer.writeValueAsString(userRequestDTO);
		RequestBuilder request = MockMvcRequestBuilders.put("/api/user/edit").content(requestJson)
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(request).andExpect(status().isBadRequest()).andReturn();
	}

	@Test
	void testFindPositive() throws Exception {
		mock(PermissionRepository.class);
		when(userService.getUserById(1L)).thenReturn(userEntity);
		ResponseUtil.mapUserEntitytoResponse(userEntity);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/user/{id}", 1L)).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testFindNegative() throws Exception {
		mock(PermissionRepository.class);
		when(userService.getUserById(5L)).thenReturn(userEntity);
		userEntity.setIsDeleted(true);
		boolean userId = false;
		try {
			if (user.isPresent() && !user.get().getIsDeleted()) {
			}
			else {
				throw new ServiceException(messageSource.getMessage(MessageConstants.ERROR_USER_NOT_FOUND,
						new Object[] { 5 }, LocaleContextHolder.getLocale()));
			}

		} catch (Exception e) {
			userId = true;
		}

		assertTrue(userId);
	}

	@Test
	void testFindAll()throws Exception  {
		when(userService.fetchList()).thenReturn(usersList);
		ResponseUtil.mapUserEntityListtoResponse(usersList);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/user")).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testGetUserByEmail()throws Exception {
		when(userService.getUserByEmail("kittu1@aven-sys.com")).thenReturn(userEntity);
		ResponseUtil.mapUserEntitytoResponse(userEntity);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/user/email/{email}","kittu1@aven-sys.com")).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testGetUsersUnderManager() throws Exception {
		userService.getAllUsersUnderManagerQuery();
		mockMvc.perform(MockMvcRequestBuilders.get("/api/user/users-under-manager")).andExpect(status().isOk())
				.andReturn();
	}

	@Test
	void testGetUserDetail() throws Exception{
		when(userService.getUserDetail()).thenReturn(userEntity);
		ResponseUtil.mapUserEntitytoResponse(userEntity);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile")).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testDeleteUser() throws Exception {
		userService.delete(1L);
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/{id}/delete", 1)).andExpect(status().isOk())
				.andReturn();
	}

}
