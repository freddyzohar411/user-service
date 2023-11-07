package com.avensys.rts.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.avensys.rts.userservice.entity.UserEntity;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

	Optional<UserEntity> findByEmail(String email);

	Optional<UserEntity> findByUsernameOrEmail(String username, String email);

	Optional<UserEntity> findByUsername(String username);

	Optional<UserEntity> findByEmployeeId(String employeeId);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

	Boolean existsByEmployeeId(String employeeId);
	
	@Query(value = "SELECT group FROM UserEntity group WHERE group.isDeleted = ?1")
	List<UserEntity> findAllAndIsDeleted(boolean isDeleted);

	@Query(value = "SELECT u from UserEntity u WHERE u.isDeleted = ?1 AND u.isActive = ?2")
	Page<UserEntity> findAllByPaginationAndSort(Boolean isDeleted, Boolean isActive, Pageable pageable);

	Page<UserEntity> findAll(Specification<UserEntity> specification, Pageable pageable);

}