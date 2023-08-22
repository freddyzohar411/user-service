package com.avensys.rts.userservice.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avensys.rts.userservice.entity.RoleEntity;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.payload.LoginDTO;
import com.avensys.rts.userservice.repository.RoleRepository;
import com.avensys.rts.userservice.repository.UserRepository;
import com.avensys.rts.userservice.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/signin")
	public ResponseEntity<String> authenticateUser(@RequestBody LoginDTO loginDTO) {
		try {
			UserDetails user = userService.loadUserByUsername(loginDTO.getUsernameOrEmail());
			System.out.println("test "+user.getPassword()+" "+passwordEncoder.encode(loginDTO.getPassword()));
			if (user.getPassword().equals(passwordEncoder.encode(loginDTO.getPassword()))) {
				return new ResponseEntity<>("User signed-in successfully!.", HttpStatus.OK);
			} else {
				System.out.println("test username "+user.getPassword()+" "+passwordEncoder.encode(loginDTO.getPassword()));
				return new ResponseEntity<>("Invalid username/password.", HttpStatus.UNAUTHORIZED);
			}
		} catch (UsernameNotFoundException e) {
			return new ResponseEntity<>("Username/Email not found.", HttpStatus.UNAUTHORIZED);
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody UserEntity user) {

		// add check for username exists in a DB
		if (userRepository.existsByUsername(user.getUsername())) {
			return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
		}

		// add check for email exists in DB
		if (userRepository.existsByEmail(user.getEmail())) {
			return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
		}

		// create user object
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		RoleEntity roles = roleRepository.findByName("ROLE_ADMIN").get();
		user.setRoles(Collections.singleton(roles));

		userRepository.save(user);

		return new ResponseEntity<>("User registered successfully", HttpStatus.OK);

	}
}
