package com.avensys.rts.userservice.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avensys.rts.userservice.api.exception.PasswordMismatchException;
import com.avensys.rts.userservice.api.exception.ServiceException;
import com.avensys.rts.userservice.api.exception.TokenInvalidException;
import com.avensys.rts.userservice.constants.MessageConstants;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.payload.ForgetResetPasswordRequestDTO;
import com.avensys.rts.userservice.payload.InstrospectResponseDTO;
import com.avensys.rts.userservice.payload.LoginDTO;
import com.avensys.rts.userservice.payload.LoginResponseDTO;
import com.avensys.rts.userservice.payload.LogoutResponseDTO;
import com.avensys.rts.userservice.payload.RefreshTokenDTO;
import com.avensys.rts.userservice.payload.ResetLoginRequestDTO;
import com.avensys.rts.userservice.payload.UserListingRequestDTO;
import com.avensys.rts.userservice.payload.UserRequestDTO;
import com.avensys.rts.userservice.service.UserService;
import com.avensys.rts.userservice.util.JwtUtil;
import com.avensys.rts.userservice.util.PasswordUtil;
import com.avensys.rts.userservice.util.ResponseUtil;

@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginDTO) {

		String decodedPassword = PasswordUtil.decode(loginDTO.getPassword());
		loginDTO.setPassword(decodedPassword);

		Authentication authenticate = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
		try {
			if (authenticate.isAuthenticated()) {
				LoginResponseDTO response = userService.login(loginDTO);

				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				return ResponseUtil.generateSuccessResponse(null, HttpStatus.UNAUTHORIZED, messageSource.getMessage(
						MessageConstants.ERROR_USER_EMAIL_NOT_FOUND, null, LocaleContextHolder.getLocale()));
			}
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.UNAUTHORIZED, messageSource
					.getMessage(MessageConstants.ERROR_USER_EMAIL_NOT_FOUND, null, LocaleContextHolder.getLocale()));
		}
	}

	@PostMapping("/refreshToken")
	public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
		try {
			userService.getUserById(refreshTokenDTO.getId());
			LoginResponseDTO response = userService.refreshToken(refreshTokenDTO);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.UNAUTHORIZED, messageSource
					.getMessage(MessageConstants.ERROR_USER_NOT_FOUND, null, LocaleContextHolder.getLocale()));
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody UserRequestDTO user) {
		try {
			// create user object
			userService.saveUser(user, null);

			return ResponseUtil.generateSuccessResponse(null, HttpStatus.CREATED,
					messageSource.getMessage(MessageConstants.USER_REGISTERED, null, LocaleContextHolder.getLocale()));
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.BAD_REQUEST, messageSource
					.getMessage(MessageConstants.ERROR_EMAIL_TAKEN, null, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * @author Rahul Sahu
	 * @param resetLoginRequestDTO
	 * @return
	 * @description First time login user password reset
	 */
	@PostMapping("/loginResetPassword")
	public ResponseEntity<?> firstTimeLoginResetPassword(@RequestBody ResetLoginRequestDTO resetLoginRequestDTO) {
		try {
			userService.loginResetPassword(resetLoginRequestDTO);

			return ResponseUtil.generateSuccessResponse(null, HttpStatus.CREATED,
					messageSource.getMessage(MessageConstants.USER_UPDATED, null, LocaleContextHolder.getLocale()));
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
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

	@PostMapping("/add")
	public ResponseEntity<?> createUser(@RequestBody UserRequestDTO user,
			@RequestHeader(name = "Authorization") String token) {
		try {
			String decodedPassword = PasswordUtil.decode(user.getPassword());
			user.setPassword(decodedPassword);

			Long userId = jwtUtil.getUserId(token);
			userService.saveUser(user, userId);
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.CREATED,
					messageSource.getMessage(MessageConstants.USER_CREATED, null, LocaleContextHolder.getLocale()));

		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PutMapping("/edit")
	public ResponseEntity<?> editUser(@RequestBody UserRequestDTO user,
			@RequestHeader(name = "Authorization") String token) {
		try {
			String decodedPassword = PasswordUtil.decode(user.getPassword());
			user.setPassword(decodedPassword);

			Long userId = jwtUtil.getUserId(token);
			userService.update(user, userId);
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK,
					messageSource.getMessage(MessageConstants.USER_UPDATED, null, LocaleContextHolder.getLocale()));
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@DeleteMapping("/{id}/delete")
	public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
		try {
			userService.delete(id);
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK,
					messageSource.getMessage(MessageConstants.USER_DELETED, null, LocaleContextHolder.getLocale()));
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> find(@PathVariable("id") Long id) {
		try {
			UserEntity user = userService.getUserById(id);
			return ResponseUtil.generateSuccessResponse(ResponseUtil.mapUserEntitytoResponse(user), HttpStatus.OK,
					null);
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<?> findAll() {
		List<UserEntity> users = userService.fetchList();
		return ResponseUtil.generateSuccessResponse(ResponseUtil.mapUserEntityListtoResponse(users), HttpStatus.OK,
				null);

	}

	@GetMapping("/email/{email}")
	public ResponseEntity<Object> getUserByEmail(@PathVariable String email) {
		try {
			UserEntity user = userService.getUserByEmail(email);
			return ResponseUtil.generateSuccessResponse(ResponseUtil.mapUserEntitytoResponse(user), HttpStatus.OK,
					null);
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@GetMapping("/profile")
	public ResponseEntity<Object> getUserDetail() {
		try {
			UserEntity user = userService.getUserDetail();
			return ResponseUtil.generateSuccessResponse(ResponseUtil.mapUserEntitytoResponse(user), HttpStatus.OK,
					null);
		} catch (ServiceException e) {
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@PostMapping("listing")
	public ResponseEntity<Object> getUserListing(@RequestBody UserListingRequestDTO userListingRequestDTO) {
		Integer page = userListingRequestDTO.getPage();
		Integer pageSize = userListingRequestDTO.getPageSize();
		String sortBy = userListingRequestDTO.getSortBy();
		String sortDirection = userListingRequestDTO.getSortDirection();
		String searchTerm = userListingRequestDTO.getSearchTerm();
		if (searchTerm == null || searchTerm.isEmpty()) {
			return ResponseUtil.generateSuccessResponse(
					ResponseUtil.mapUserPageToUserListingResponseDTO(
							userService.getUserListingPage(page, pageSize, sortBy, sortDirection)),
					HttpStatus.OK,
					messageSource.getMessage(MessageConstants.USER_SUCCESS, null, LocaleContextHolder.getLocale()));
		}
		return ResponseUtil.generateSuccessResponse(
				ResponseUtil.mapUserPageToUserListingResponseDTO(
						userService.getUserListingPageWithSearch(page, pageSize, sortBy, sortDirection, searchTerm)),
				HttpStatus.OK,
				messageSource.getMessage(MessageConstants.USER_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	@GetMapping("/users-under-manager")
	public ResponseEntity<Object> getUsersUnderManager() throws ServiceException {
		return ResponseUtil.generateSuccessResponse(userService.getAllUsersUnderManagerQuery(), HttpStatus.OK, null);
	}

	@GetMapping("/forget-password/{email}")
	public ResponseEntity<Object> forgetPassword(@PathVariable String email) {
		try {
			userService.forgetPassword(email);
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK, messageSource
					.getMessage(MessageConstants.USER_FORGET_EMAIL_NOT_SENT, null, LocaleContextHolder.getLocale()));
		} catch (ServiceException e) {
			return ResponseUtil.generateErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@GetMapping("/validate-forget-password-token")
	public ResponseEntity<Object> validateForgetPasswordToken(@RequestParam String token) {
		Boolean isTokenValid = userService.validateForgetPasswordToken(token);
		if (!isTokenValid) {
			return ResponseUtil.generateSuccessResponse(isTokenValid, HttpStatus.BAD_REQUEST, messageSource.getMessage(
					MessageConstants.ERROR_USER_FORGET_PASSWORD_TOKEN_INVALID, null, LocaleContextHolder.getLocale()));
		}
		return ResponseUtil.generateSuccessResponse(isTokenValid, HttpStatus.OK, messageSource
				.getMessage(MessageConstants.USER_FORGET_PASSWORD_TOKEN_VALID, null, LocaleContextHolder.getLocale()));

	}

	@PostMapping("/forget-password/reset")
	public ResponseEntity<Object> forgetPasswordReset(
			@RequestBody ForgetResetPasswordRequestDTO forgetResetPasswordRequestDTO) {
		try {
			userService.forgetPasswordReset(forgetResetPasswordRequestDTO);
			return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK,
					messageSource.getMessage(MessageConstants.USER_UPDATED, null, LocaleContextHolder.getLocale()));
		} catch (ServiceException e) {
			return ResponseUtil.generateErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (TokenInvalidException e) {
			return ResponseUtil.generateErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (PasswordMismatchException e) {
			return ResponseUtil.generateErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

}
