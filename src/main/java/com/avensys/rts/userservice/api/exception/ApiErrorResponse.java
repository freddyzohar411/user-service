package com.avensys.rts.userservice.api.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ApiErrorResponse {
	private final String guid;
	private final String errorCode;
	private final String message;
	private final Integer statusCode;
	private final String statusName;
	private final String path;
	private final String method;
	private final LocalDateTime timestamp;
}