package com.avensys.rts.userservice.APIClient;

import com.avensys.rts.userservice.entity.UserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.avensys.rts.userservice.interceptor.JwtTokenInterceptor;
import com.avensys.rts.userservice.payload.UserAddUserGroupsRequestDTO;
import com.avensys.rts.userservice.response.HttpResponse;

@Configuration
@FeignClient(name = "usergroup-service", url = "${api.usergroup.url}", configuration = JwtTokenInterceptor.class)
public interface UserGroupAPIClient {

	@PostMapping("/add-usergroups")
	HttpResponse addUserGroupsToUser(UserAddUserGroupsRequestDTO userAddUserGroupRequestDTO);
}
