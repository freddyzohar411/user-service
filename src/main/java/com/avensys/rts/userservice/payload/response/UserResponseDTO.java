package com.avensys.rts.userservice.payload.response;

import java.time.LocalDateTime;
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
	private Boolean isTemp;
	private List<UserGroupResponseDTO> userGroup;

	// Added by He Xiang 11122023
	private Long managerId;
	private LocalDateTime createdAt;
	private UserResponseDTO Manager;

	// Added 29022024 - Koh He Xiang
	private String location;
	private String country;
	private String designation;

	//Added 29022024 - Koh He Xiang
	private Boolean status;
}
