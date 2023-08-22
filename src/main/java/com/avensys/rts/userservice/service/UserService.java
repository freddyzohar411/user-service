package com.avensys.rts.userservice.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.avensys.rts.userservice.entity.UserEntity;

public interface UserService extends UserDetailsService {

	public void saveUser(UserEntity user);

}