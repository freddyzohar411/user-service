package com.avensys.rts.userservice.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.avensys.rts.userservice.entity.ModuleEntity;
import com.avensys.rts.userservice.entity.PermissionEntity;
import com.avensys.rts.userservice.entity.RoleEntity;
import com.avensys.rts.userservice.entity.UserEntity;
import com.avensys.rts.userservice.payload.response.ModuleResponseDTO;
import com.avensys.rts.userservice.payload.response.RoleResponseDTO;
import com.avensys.rts.userservice.payload.response.UserGroupResponseDTO;
import com.avensys.rts.userservice.payload.response.UserListingResponseDTO;
import com.avensys.rts.userservice.payload.response.UserResponseDTO;
import com.avensys.rts.userservice.repository.PermissionRepository;
import com.avensys.rts.userservice.response.HttpResponse;

@Component
public class ResponseUtil {

	private static PermissionRepository permissionRepository;

	@Autowired
	public void setSomeThing(PermissionRepository permissionRepository) {
		ResponseUtil.permissionRepository = permissionRepository;
	}

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
				groupResponseDTO.setId(group.getId()); //Added by HX
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

		Map<Long, String> permissionsMap = new HashMap<Long, String>();
		Iterable<PermissionEntity> permissionEntities = permissionRepository.findAll();

		if (permissionEntities.spliterator().getExactSizeIfKnown() > 0) {
			permissionEntities.forEach(per -> {
				permissionsMap.put(per.getId(), per.getPermissionName());
			});
		}

		if (role.getModulePermissions() != null && role.getModulePermissions().size() > 0) {
			role.getModulePermissions().forEach(modulePermission -> {
				ModuleEntity module = modulePermission.getModule();
				ModuleResponseDTO moduleResponseDTO = new ModuleResponseDTO();
				moduleResponseDTO.setId(module.getId());
				moduleResponseDTO.setModuleName(module.getModuleName());

				List<String> permissions = new ArrayList<String>();
				if (modulePermission.getPermissions() != null && modulePermission.getPermissions().length() > 0) {
					String[] permissionIds = modulePermission.getPermissions().split(",");
					for (int i = 0; i < permissionIds.length; i++) {
						Long id = Long.parseLong(permissionIds[i]);
						if (permissionsMap.get(id) != null) {
							permissions.add(permissionsMap.get(id));
						}
					}
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

	public static UserListingResponseDTO mapUserPageToUserListingResponseDTO(Page<UserEntity> userEntityPage) {
		UserListingResponseDTO userListingResponseDTO = new UserListingResponseDTO();
		userListingResponseDTO.setPage(userEntityPage.getNumber());
		userListingResponseDTO.setPageSize(userEntityPage.getSize());
		userListingResponseDTO.setTotalElements(userEntityPage.getTotalElements());
		userListingResponseDTO.setTotalPages(userEntityPage.getTotalPages());
		userListingResponseDTO.setUsers(mapUserEntityListtoResponse(userEntityPage.getContent()));
		return userListingResponseDTO;
	}

}
