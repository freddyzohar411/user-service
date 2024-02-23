package com.avensys.rts.userservice.payload;

import java.time.LocalDateTime;
import java.util.Set;

import com.avensys.rts.userservice.entity.RoleEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
	private long id;
	private String firstName;
	private String lastName;
	private String username;
	private String email;
	private String mobile;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Boolean locked;
	private Boolean enabled;
	private Set<RoleEntity> roles;
}
