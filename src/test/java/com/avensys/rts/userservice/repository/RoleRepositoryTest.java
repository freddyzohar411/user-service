package com.avensys.rts.userservice.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.avensys.rts.userservice.entity.RoleEntity;
import com.avensys.rts.userservice.entity.RoleModulePermissionsEntity;
import com.avensys.rts.userservice.entity.UserGroupEntity;

public class RoleRepositoryTest {

	@Mock
	private RoleRepository roleRepository;

	RoleEntity roleEntity;
	Optional<RoleEntity> roleEntityOptional;
	RoleModulePermissionsEntity roleModulePermissionsEntity;
	RoleModulePermissionsEntity roleModulePermissionsEntity1;
	Set<RoleModulePermissionsEntity> modulePermissions;
	Set<UserGroupEntity> groupEntities;
	@MockBean
	AutoCloseable autoCloseable;

	@BeforeEach
	void setUp() {
		autoCloseable = MockitoAnnotations.openMocks(this);
		modulePermissions = new HashSet<RoleModulePermissionsEntity>();
		modulePermissions.add(roleModulePermissionsEntity);
		modulePermissions.add(roleModulePermissionsEntity1);
		roleEntity = new RoleEntity(1L, "Super Admin", "Super admin access to all modules!", modulePermissions,
				groupEntities);
		roleEntityOptional = Optional.of(roleEntity);
		roleRepository.save(roleEntity);
	}

	@AfterEach
	void tearDown() throws Exception {
		roleEntity = null;
		roleRepository.deleteAll();

	}

	@Test
	public void testFindByRoleName() throws Exception {
		mock(RoleRepository.class);
		Optional<RoleEntity> result = roleRepository.findByRoleName("Super Admin");
		when(roleRepository.findByRoleName("Super Admin")).thenReturn(roleEntityOptional);
		assertNotNull(result);
	}
}
