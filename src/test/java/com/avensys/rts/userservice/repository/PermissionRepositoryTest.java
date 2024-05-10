package com.avensys.rts.userservice.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.avensys.rts.userservice.entity.PermissionEntity;

public class PermissionRepositoryTest {

	@Mock
	private PermissionRepository permissionRepository;

	PermissionEntity permissionEntity;
	PermissionEntity permissionEntity1;
	Optional<PermissionEntity> permission;

	@MockBean
	AutoCloseable autoCloseable;

	@BeforeEach
	void setUp() {
		autoCloseable = MockitoAnnotations.openMocks(this);
		permissionEntity = new PermissionEntity(1L, "Read", "Read and view data.");
		permission = Optional.of(permissionEntity);

	}

	@AfterEach
	void tearDown() throws Exception {
		autoCloseable.close();
	}

	@Test
	void testFindByPermissionName() {
		mock(PermissionRepository.class);
		when(permissionRepository.findByPermissionName("Read")).thenReturn(permission);
		assertNotNull(permission);

	}

}
