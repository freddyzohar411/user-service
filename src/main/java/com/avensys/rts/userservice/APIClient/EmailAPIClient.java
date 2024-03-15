package com.avensys.rts.userservice.APIClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.avensys.rts.userservice.payload.EmailMultiRequestDTO;
import com.avensys.rts.userservice.payload.EmailMultiTemplateRequestDTO;
import com.avensys.rts.userservice.response.HttpResponse;

/**
 * @author Rahul Sahu
 * @description This class is an interface to interact with document
 *              microservice
 */
@Configuration
@FeignClient(name = "email-service", url = "${api.email.url}")
public interface EmailAPIClient {

	@PostMapping("/sendingEmail-service")
	public HttpResponse sendEmailService(@RequestBody EmailMultiRequestDTO emailMultiRequestDTO);

	@PostMapping("/sendingEmail-service/template")
	public HttpResponse sendEmailServiceTemplate(
			@RequestBody EmailMultiTemplateRequestDTO emailMultiTemplateRequestDTO);

}
