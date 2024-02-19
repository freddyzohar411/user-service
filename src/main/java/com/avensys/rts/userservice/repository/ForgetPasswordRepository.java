package com.avensys.rts.userservice.repository;

import com.avensys.rts.userservice.entity.ForgetPasswordEntity;
import com.avensys.rts.userservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ForgetPasswordRepository extends JpaRepository<ForgetPasswordEntity, Long> {
	ForgetPasswordEntity findByToken(String token);

	// Find by User, expired and not used
	@Query("SELECT f FROM ForgetPassword f WHERE f.user = ?1 AND f.isUsed = false AND f.expiryTime > CURRENT_TIMESTAMP")
	Optional<ForgetPasswordEntity> findByUserAndIsUsedFalseAndExpiryTimeAfter(UserEntity userEntity);

}
