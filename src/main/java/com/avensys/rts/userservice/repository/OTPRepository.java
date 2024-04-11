package com.avensys.rts.userservice.repository;

import com.avensys.rts.userservice.entity.OTPEnity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OTPRepository extends JpaRepository<OTPEnity, Long> {

}
