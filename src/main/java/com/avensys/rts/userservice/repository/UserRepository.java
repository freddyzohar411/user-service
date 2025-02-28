package com.avensys.rts.userservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.avensys.rts.userservice.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

	Optional<UserEntity> findByEmail(String email);

	Optional<UserEntity> findByUsernameOrEmailIgnoreCase(String username, String email);

	Optional<UserEntity> findByUsername(String username);

	Optional<UserEntity> findByEmployeeId(String employeeId);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

	Boolean existsByEmployeeId(String employeeId);

	@Query(value = "SELECT group FROM UserEntity group WHERE group.isDeleted = ?1")
	List<UserEntity> findAllAndIsDeleted(boolean isDeleted);

	@Query(value = "SELECT u from UserEntity u WHERE u.isDeleted = ?1")
	Page<UserEntity> findAllByIsInDeletedPaginationAndSort(Boolean isDeleted, Pageable pageable);

	@Query(value = "SELECT u from UserEntity u WHERE u.isDeleted = ?1 AND u.isActive = ?2")
	Page<UserEntity> findAllByPaginationAndSort(Boolean isDeleted, Boolean isActive, Pageable pageable);

	Page<UserEntity> findAll(Specification<UserEntity> specification, Pageable pageable);

	@Query(value = "WITH RECURSIVE UserHierarchy AS (SELECT id, manager FROM users WHERE id = :userId UNION "
			+ "SELECT u.id, u.manager FROM users u JOIN UserHierarchy h ON u.manager = h.id) "
			+ "SELECT id FROM UserHierarchy WHERE id IS NOT NULL", nativeQuery = true)
	Set<Long> findUserIdsUnderManager(@Param("userId") Long userId);

	@Query(value = "WITH RECURSIVE UserHierarchy AS (" + "SELECT * FROM users WHERE id = :userId " + "UNION "
			+ "SELECT u.* FROM users u JOIN UserHierarchy h ON u.manager = h.id) "
			+ "SELECT * FROM UserHierarchy WHERE id IS NOT NULL", nativeQuery = true)
	Set<UserEntity> findUserEntitiesUnderManager(@Param("userId") Long userId);


	@Query(value = "SELECT u FROM UserEntity u WHERE u.id IN :userIds AND u.isDeleted = :isDeleted AND u.isActive = :isActive")
	List<UserEntity> findUserInIdsAndIsDeletedAndIsActive(List<Long> userIds, Boolean isDeleted, Boolean isActive);

	@Query(value = "SELECT u.isDeleted FROM UserEntity u WHERE u.username = :username")
	Boolean findIsDeletedByUsername(@Param("username") String username);

}