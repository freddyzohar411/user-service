package com.avensys.rts.userservice.interceptor;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuditInterceptor implements HandlerInterceptor {
	private final Logger LOGGER = LoggerFactory.getLogger(AuditInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		LOGGER.info("Pre-handling request");

		// Get In-time in milliseconds and set in request context
		request.setAttribute("startTime", System.currentTimeMillis());

		// Get Thread ID and set in request context
		String threadId = RandomStringUtils.randomAlphanumeric(10);
		request.setAttribute("threadId", threadId);

		return true; // Continue the request processing chain
	}
}
