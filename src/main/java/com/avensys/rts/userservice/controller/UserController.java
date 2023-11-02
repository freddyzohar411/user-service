package com.avensys.rts.userservice.controller;

import java.util.List;

import com.avensys.rts.userservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.avensys.rts.userservice.api.exception.ServiceException;
import com.avensys.rts.userservice.constants.MessageConstants;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.payload.InstrospectResponseDTO;
import com.avensys.rts.userservice.payload.LoginDTO;
import com.avensys.rts.userservice.payload.LoginResponseDTO;
import com.avensys.rts.userservice.payload.LogoutResponseDTO;
import com.avensys.rts.userservice.service.UserService;
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

//	@PostMapping("/signin")
//	public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginDTO) {
//		Authentication authenticate = authenticationManager
//				.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
//		if (authenticate.isAuthenticated()) {
//			LoginResponseDTO response = userService.login(loginDTO);
//
//			return new ResponseEntity<>(response, HttpStatus.OK);
//		} else {
//			return ResponseUtil.generateSuccessResponse(null, HttpStatus.UNAUTHORIZED, messageSource
//					.getMessage(MessageConstants.ERROR_USER_EMAIL_NOT_FOUND, null, LocaleContextHolder.getLocale()));
//		}
//	}

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
            return ResponseUtil.generateSuccessResponse(null, HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> editUser(@RequestBody UserEntity user) {
        try {
            userService.update(user);
            return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK,
                    messageSource.getMessage(MessageConstants.USER_UPDATED, null, LocaleContextHolder.getLocale()));
        } catch (ServiceException e) {
            return ResponseUtil.generateSuccessResponse(null, HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
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

    @GetMapping("/all")
    public ResponseEntity<?> findAll() {
        List<UserEntity> users = userService.fetchList();
        return ResponseUtil.generateSuccessResponse(ResponseUtil.mapUserEntityListtoResponse(users), HttpStatus.OK,
                null);

    }

    @GetMapping("")
    public ResponseEntity<Object> getUserByEmail(@RequestParam(required = false) String email) {
        if (email == null) {
            List<UserEntity> users = userService.fetchList();
            return ResponseUtil.generateSuccessResponse(ResponseUtil.mapUserEntityListtoResponse(users), HttpStatus.OK,
                    null);
        }
        return ResponseUtil.generateSuccessResponse(userService.getUserByEmail(email), HttpStatus.OK, messageSource.getMessage(MessageConstants.USER_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Object> getUserDetailByEmail(@PathVariable("email") String email) {
        return ResponseUtil.generateSuccessResponse(ResponseUtil.mapUserEntitytoResponse(userService.getUserByEmail(email)), HttpStatus.OK, messageSource.getMessage(MessageConstants.USER_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    @GetMapping("/profile")
    public ResponseEntity<Object> getUserDetail() {
        return ResponseUtil.generateSuccessResponse(ResponseUtil.mapUserEntitytoResponse(userService.getUserDetail()), HttpStatus.OK, messageSource.getMessage(MessageConstants.USER_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

}
