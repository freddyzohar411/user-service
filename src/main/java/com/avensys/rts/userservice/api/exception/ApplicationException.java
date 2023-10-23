package com.avensys.rts.userservice.api.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final String errorCode;
	private final String message;
	private final HttpStatus httpStatus;
}