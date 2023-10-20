package com.avensys.rts.userservice.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.avensys.rts.userservice.response.HttpResponse;

public class ResponseUtil {
	public static ResponseEntity<Object> generateSuccessResponse(Object dataObject, HttpStatus httpStatus,
			String message) {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setData(dataObject);
		httpResponse.setCode(httpStatus.value());
		httpResponse.setMessage(message);
		return new ResponseEntity<>(httpResponse, httpStatus);
	}

	public static ResponseEntity<Object> generateErrorResponse(HttpStatus httpStatus, String message) {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setCode(httpStatus.value());
		httpResponse.setError(true);
		httpResponse.setMessage(message);
		return new ResponseEntity<>(httpResponse, httpStatus);
	}

}
