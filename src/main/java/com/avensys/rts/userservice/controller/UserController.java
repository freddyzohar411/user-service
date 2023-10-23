package com.avensys.rts.userservice.controller;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avensys.rts.userservice.api.exception.ServiceException;
import com.avensys.rts.userservice.constants.MessageConstants;
import com.avensys.rts.userservice.entity.RoleEntity;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.payload.InstrospectResponseDTO;
import com.avensys.rts.userservice.payload.LoginDTO;
import com.avensys.rts.userservice.payload.LoginResponseDTO;
import com.avensys.rts.userservice.payload.LogoutResponseDTO;
import com.avensys.rts.userservice.repository.RoleRepository;
import com.avensys.rts.userservice.service.UserService;
import com.avensys.rts.userservice.util.ResponseUtil;

@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private MessageSource messageSource;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginDTO) {
		Authentication authenticate = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
		if (authenticate.isAuthenticated()) {
			LoginResponseDTO response = userService.login(loginDTO);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.UNAUTHORIZED, messageSource
					.getMessage(MessageConstants.ERROR_USER_EMAIL_NOT_FOUND, null, LocaleContextHolder.getLocale()));
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody UserEntity user) {
		try {
			// create user object
			RoleEntity roles = roleRepository.findByName("ROLE_ADMIN").get();
			user.setRoles(Collections.singleton(roles));

			userService.saveUser(user);

			return ResponseUtil.generateSuccessResponse(null, HttpStatus.CREATED,
					messageSource.getMessage(MessageConstants.USER_REGISTERED, null, LocaleContextHolder.getLocale()));
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.BAD_REQUEST, messageSource
					.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null, LocaleContextHolder.getLocale()));
		}
	}

	@GetMapping("/logout")
	public ResponseEntity<LogoutResponseDTO> logout(@RequestParam("token") String token) {
		LogoutResponseDTO logoutResponseDTO = userService.logout(token);
		return ResponseEntity.ok(logoutResponseDTO);
	}

	@GetMapping("/validate")
	public ResponseEntity<InstrospectResponseDTO> validate(@RequestParam("token") String token) {
		InstrospectResponseDTO instrospectResponseDTO = userService.validate(token);
		return ResponseEntity.ok(instrospectResponseDTO);
	}

	@PostMapping
	public ResponseEntity<?> createUser(@RequestBody UserEntity user) {
		try {
			userService.saveUser(user);
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.CREATED,
					messageSource.getMessage(MessageConstants.USER_CREATED, null, LocaleContextHolder.getLocale()));

		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.BAD_REQUEST, messageSource
					.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null, LocaleContextHolder.getLocale()));
		}
	}

	@PutMapping
	public ResponseEntity<?> editUser(@RequestBody UserEntity user) {
		try {
			// create user object
			RoleEntity roles = roleRepository.findByName("ROLE_ADMIN").get();
			user.setRoles(Collections.singleton(roles));
			userService.saveUser(user);

			return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.BAD_REQUEST, messageSource
					.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null, LocaleContextHolder.getLocale()));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable("id") long id) {
		Optional<UserEntity> user = userService.getUserById(id);
		if (user.isPresent() && !user.get().getIsDeleted()) {
			UserEntity dbUser = user.get();
			dbUser.setIsDeleted(true);
			userService.update(dbUser);
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK,
					messageSource.getMessage(MessageConstants.USER_DELETED, null, LocaleContextHolder.getLocale()));
		} else {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.NOT_FOUND, messageSource.getMessage(
					MessageConstants.ERROR_USER_NOT_FOUND, new Object[] { id }, LocaleContextHolder.getLocale()));
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> find(@PathVariable("id") Long id) {
		Optional<UserEntity> user = userService.getUserById(id);
		if (user.isPresent() && !user.get().getIsDeleted()) {
			return ResponseUtil.generateSuccessResponse(user.get(), HttpStatus.OK, null);
		} else {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.NOT_FOUND, messageSource.getMessage(
					MessageConstants.ERROR_USER_NOT_FOUND, new Object[] { id }, LocaleContextHolder.getLocale()));
		}
	}

}
