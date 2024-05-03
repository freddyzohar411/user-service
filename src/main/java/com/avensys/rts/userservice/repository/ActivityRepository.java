package com.avensys.rts.userservice.repository;

import com.avensys.rts.userservice.entity.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {
}
