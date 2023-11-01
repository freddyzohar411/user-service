package com.avensys.rts.userservice.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.avensys.rts.userservice.entity.UserEntity;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

	Optional<UserEntity> findByEmail(String email);

	Optional<UserEntity> findByUsernameOrEmail(String username, String email);

	Optional<UserEntity> findByUsername(String username);

	Optional<UserEntity> findByEmployeeId(String employeeId);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

	Boolean existsByEmployeeId(String employeeId);

}