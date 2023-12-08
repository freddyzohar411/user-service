package com.avensys.rts.userservice.payload.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupResponseDTO {
	private Long id;
	private String groupName;
	private String groupDescription;
	private List<RoleResponseDTO> roles;
}
