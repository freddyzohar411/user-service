package com.avensys.rts.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avensys.rts.userservice.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByEmail(String email);

	Optional<UserEntity> findByUsernameOrEmail(String username, String email);

	Optional<UserEntity> findByUsername(String username);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

}