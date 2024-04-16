package com.avensys.rts.userservice.repository;

import com.avensys.rts.userservice.entity.OTPEnity;
import com.avensys.rts.userservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTPEnity, Long> {
	@Query("SELECT otp FROM otp otp WHERE otp.user = :user AND otp.otpToken = :otpToken")
	Optional<OTPEnity> findByUserAndOTPToken(UserEntity user, String otpToken);
}
