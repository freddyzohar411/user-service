package com.avensys.rts.userservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;

import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.entity.UserGroupEntity;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserRepositoryTest {

	@Mock
	UserRepository userRepository;

	UserEntity userEntity;

	UserEntity userEntity1;

	Optional<UserEntity> entityOptional;

	@MockBean
	AutoCloseable autoCloseable;

	Set<UserGroupEntity> groupEntities;
	Set<UserEntity> users;
	UserEntity manager;
	List<UserEntity> userList;
	Pageable pageable = null;
	Pageable pageableAsc = null;
	Specification<UserEntity> specification;
	Page<UserEntity> userPage;
	Page<UserEntity> userPageAsc;
	Set<Long> ids;
	Sort sortDec = null;
	Sort sortAsc = null;

	@BeforeEach
	void setUp() {
		autoCloseable = MockitoAnnotations.openMocks(this);
		userEntity = new UserEntity(1L, "339f35a7-0d3d-431e-9a63-d90d4c342e4a", "Kotaiah", "Nalleboina",
				"kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", false, true,
				groupEntities, users, manager);
		userEntity1 = new UserEntity(2L, "339f35a7-0d3d-431e-9a63-d90d4c342e4a", "Kittu", "Nalleb",
				"kittu1@aven-sys.com", "kittu1@aven-sys.com",
				"$2a$10$pxSQVx/EqvfrehZDdN6Q3.Qg3Agm2S/d60xYqy0rFpuNSgt1DcpvO", "9381515362", "234", false, true,
				groupEntities, users, manager);
		entityOptional = Optional.of(userEntity);
		userList = Arrays.asList(userEntity, userEntity1);
		sortDec = Sort.by(Sort.Direction.DESC, "updatedAt");
		sortAsc = Sort.by(Sort.Direction.ASC, "updatedAt");
		pageableAsc = PageRequest.of(1, 2, sortAsc);
		pageable = PageRequest.of(1, 2, sortDec);
		userPageAsc = new PageImpl<UserEntity>(userList, pageableAsc, 2);
		userPage = new PageImpl<UserEntity>(userList, pageable, 2);
		users = new HashSet<UserEntity>();
		users.add(userEntity);
		users.add(userEntity1);
		ids = new HashSet<Long>();
		ids.add(2L);
		ids.add(3L);

	}

	@AfterEach
	void tearDown() throws Exception {

	}

	@Test
	@Order(1)
	@Rollback(value = false)
	public void testFindByEmail() {
		mock(UserRepository.class);
		userRepository.save(userEntity);
		when(userRepository.findByEmail("kittu1@aven-sys.com")).thenReturn(entityOptional);
		assertThat(userRepository.findByEmail("kittu1@aven-sys.com")).isEqualTo(entityOptional);
		assertNotNull(entityOptional);

	}

	@Test
	@Order(2)
	@Rollback(value = false)
	public void testFindByUsernameOrEmail() {
		mock(UserRepository.class);
		when(userRepository.findByUsernameOrEmail("Kotaiah", "kittu1@aven-sys.com")).thenReturn(entityOptional);
		assertNotNull(entityOptional);
	}

	@Test
	@Order(3)
	@Rollback(value = false)
	public void testFindByUsername() {
		mock(UserRepository.class);
		when(userRepository.findByUsername("Kotaiah")).thenReturn(entityOptional);
		assertNotNull(entityOptional);
	}

	@Test
	@Order(4)
	@Rollback(value = false)
	public void testFindByEmployeeId() {
		mock(UserRepository.class);
		when(userRepository.findByEmployeeId("123")).thenReturn(entityOptional);
		assertNotNull(entityOptional);
	}

	@Test
	@Order(5)
	@Rollback(value = false)
	public void testExistsByUsername() {
		mock(UserRepository.class);
		boolean nameExits = true;
		when(userRepository.existsByUsername("123")).thenReturn(nameExits);
		assertTrue(nameExits);
	}

	@Test
	@Order(6)
	@Rollback(value = false)
	public void testExistsByEmail() {
		mock(UserRepository.class);
		boolean emailExits = true;
		when(userRepository.existsByEmail("kittu1@aven-sys.com")).thenReturn(emailExits);
		assertTrue(emailExits);
	}

	@Test
	@Order(7)
	@Rollback(value = false)
	public void testExistsByEmployeeId() {
		mock(UserRepository.class);
		boolean idExits = true;
		when(userRepository.existsByEmployeeId("123")).thenReturn(idExits);
		assertTrue(idExits);
	}

	@Test
	@Order(8)
	@Rollback(value = false)
	public void testFindAllAndIsDeleted() {
		mock(UserRepository.class);
		when(userRepository.findAllAndIsDeleted(false)).thenReturn(userList);
		assertNotNull(userList);
	}

	@Test
	@Order(9)
	@Rollback(value = false)
	public void testFindAllByPaginationAndSort() {
		mock(UserRepository.class);
		when(userRepository.findAllByPaginationAndSort(false, true, pageable)).thenReturn(userPage);
		assertNotNull(userPage);
	}

	@Test
	@Order(10)
	@Rollback(value = false)
	public void testFindAll() {
		mock(UserRepository.class);
		when(userRepository.findAll(specification, pageable)).thenReturn(userPage);
		assertNotNull(userPage);
	}

	@Test
	@Order(11)
	@Rollback(value = false)
	public void testFindUserIdsUnderManager() {
		mock(UserRepository.class);
		when(userRepository.findUserIdsUnderManager(1L)).thenReturn(ids);
		assertThat(ids.isEmpty()).isEqualTo(false);
	}

}
