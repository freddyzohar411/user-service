package com.avensys.rts.userservice.APIClient;

import com.avensys.rts.userservice.payload.EmailMultiRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.avensys.rts.userservice.interceptor.JwtTokenInterceptor;
import com.avensys.rts.userservice.response.HttpResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * author Koh He Xiang
 * This class is an interface to interact with document microservice
 */
@Configuration
@FeignClient(name = "email-service", url = "${api.email.url}")
public interface EmailAPIClient {
    @PostMapping("/sendingEmail-service")
    HttpResponse sendEmail(@RequestBody EmailMultiRequestDTO emailMultiRequestDTO);
}
