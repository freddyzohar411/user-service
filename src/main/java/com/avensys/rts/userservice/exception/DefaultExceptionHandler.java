package com.avensys.rts.userservice.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice
public class DefaultExceptionHandler {

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<Object> exception(Exception exception) {
		return new ResponseEntity<>("API called failed", HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
