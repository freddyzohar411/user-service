package com.avensys.rts.userservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
class ApiValidationError extends ApiSubError {
	private String object;
	private String field;
	private Object rejectedValue;
	private String message;

	ApiValidationError(String object, String message) {
		this.object = object;
		this.message = message;
	}
}