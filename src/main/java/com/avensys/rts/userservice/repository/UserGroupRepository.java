package com.avensys.rts.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.avensys.rts.userservice.entity.UserGroupEntity;

public interface UserGroupRepository extends JpaRepository<UserGroupEntity, Long>, JpaSpecificationExecutor<UserGroupEntity> {
	Boolean existsByUserGroupName(String userGroupName);

	Optional<UserGroupEntity> findByUserGroupName(String userGroupName);

	@Query(value = "SELECT group FROM UserGroupEntity group WHERE group.isDeleted = ?1")
	List<UserGroupEntity> findAllAndIsDeleted(boolean isDeleted);

	@Query(value = "SELECT u from UserGroupEntity u WHERE u.isDeleted = ?1 AND u.isActive = ?2")
	Page<UserGroupEntity> findAllByPaginationAndSort(Boolean isDeleted, Boolean isActive, Pageable pageable);

	Page<UserGroupEntity> findAll(Specification<UserGroupEntity> specification, Pageable pageable);
}
