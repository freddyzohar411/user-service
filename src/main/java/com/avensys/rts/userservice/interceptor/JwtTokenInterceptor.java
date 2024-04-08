package com.avensys.rts.userservice.interceptor;

import com.avensys.rts.userservice.util.JwtUtil;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Author: Koh He Xiang
 * This class is used to intercept the request and add the JWT token to the header
 * for open feign api calls to other microservices
 */
public class JwtTokenInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", "Bearer " + JwtUtil.getTokenFromContext());
    }
}
