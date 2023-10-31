package com.avensys.rts.userservice.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.avensys.rts.userservice.entity.RoleEntity;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.payload.response.ModuleResponseDTO;
import com.avensys.rts.userservice.payload.response.RoleResponseDTO;
import com.avensys.rts.userservice.payload.response.UserGroupResponseDTO;
import com.avensys.rts.userservice.payload.response.UserResponseDTO;
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

	public static List<UserResponseDTO> mapUserEntityListtoResponse(List<UserEntity> users) {
		List<UserResponseDTO> response = new ArrayList<UserResponseDTO>();
		if (users != null && users.size() > 0) {
			users.forEach(user -> {
				response.add(mapUserEntitytoResponse(user));
			});
		}
		return response;
	}

	public static UserResponseDTO mapUserEntitytoResponse(UserEntity user) {
		UserResponseDTO dto = new UserResponseDTO();
		dto.setId(user.getId());
		dto.setKeycloackId(user.getKeycloackId());
		dto.setFirstName(user.getFirstName());
		dto.setLastName(user.getLastName());
		dto.setUsername(user.getUsername());
		dto.setEmail(user.getEmail());
		dto.setMobile(user.getMobile());
		dto.setEmployeeId(user.getEmployeeId());
		dto.setLocked(user.getLocked());
		dto.setEnabled(user.getEnabled());

		List<UserGroupResponseDTO> userGroups = new ArrayList<UserGroupResponseDTO>();

		if (user.getGroupEntities() != null && user.getGroupEntities().size() > 0) {
			user.getGroupEntities().forEach(group -> {
				UserGroupResponseDTO groupResponseDTO = new UserGroupResponseDTO();
				groupResponseDTO.setGroupName(group.getUserGroupName());
				groupResponseDTO.setGroupDescription(group.getUserGroupDescription());

				List<RoleResponseDTO> roles = new ArrayList<RoleResponseDTO>();

				if (group.getRoleEntities() != null) {
					group.getRoleEntities().forEach(role -> {
						RoleResponseDTO roleResponseDTO = mapRoleEntitytoResponse(role);
						roles.add(roleResponseDTO);
					});
				}

				groupResponseDTO.setRoles(roles);
				userGroups.add(groupResponseDTO);
			});
		}
		dto.setUserGroup(userGroups);
		return dto;

	}

	public static RoleResponseDTO mapRoleEntitytoResponse(RoleEntity role) {
		RoleResponseDTO dto = new RoleResponseDTO();
		dto.setId(role.getId());
		dto.setRoleName(role.getRoleName());
		dto.setRoleDescription(role.getRoleDescription());

		List<ModuleResponseDTO> moduleList = new ArrayList<ModuleResponseDTO>();

		if (role.getModules() != null && role.getModules().size() > 0) {
			role.getModules().forEach(module -> {
				ModuleResponseDTO moduleResponseDTO = new ModuleResponseDTO();
				moduleResponseDTO.setId(module.getId());
				moduleResponseDTO.setModuleName(module.getModuleName());
				List<String> permissions = new ArrayList<String>();
				if (module.getPermissions() != null && module.getPermissions().size() > 0) {
					module.getPermissions().forEach(permission -> {
						permissions.add(permission.getPermissionName());
					});
				}
				moduleResponseDTO.setPermissions(permissions);
				moduleList.add(moduleResponseDTO);
			});
		}

		dto.setModules(moduleList);
		return dto;
	}

	public static List<RoleResponseDTO> mapRoleEntityListtoResponse(List<RoleEntity> roles) {
		List<RoleResponseDTO> response = new ArrayList<RoleResponseDTO>();
		if (roles != null && roles.size() > 0) {
			roles.forEach(role -> {
				response.add(mapRoleEntitytoResponse(role));
			});
		}
		return response;
	}

}
