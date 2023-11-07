package com.avensys.rts.userservice.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.avensys.rts.userservice.entity.PermissionEntity;

@Repository
public interface PermissionRepository extends CrudRepository<PermissionEntity, Long> {
	Boolean existsByPermissionName(String permissionName);

	Optional<PermissionEntity> findByPermissionName(String permissionName);
}
