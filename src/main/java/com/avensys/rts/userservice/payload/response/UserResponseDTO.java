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
public class UserResponseDTO {

	private Long id;
	private String keycloackId;
	private String firstName;
	private String lastName;
	private String username;
	private String email;
	private String mobile;
	private String employeeId;
	private Boolean locked;
	private Boolean enabled;
	private List<UserGroupResponseDTO> userGroup;
}
