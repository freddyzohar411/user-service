package com.avensys.rts.userservice.payload;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

	private Long id;

	@NotNull(message = "First Name cannot be empty")
	private String firstName;

	@NotNull(message = "Last Name cannot be empty")
	private String lastName;

	@NotNull(message = "Username cannot be empty")
	private String username;

	@NotNull(message = "Email cannot be empty")
	@Email(message = "Please enter a valid email address")
	private String email;

	@NotNull(message = "Password cannot be empty")
	@Length(min = 7, message = "Password should be atleast 7 characters long")
	private String password;

	@Length(min = 10, message = "Password should be atleast 10 number long")
	private String mobile;

	private String employeeId;

	private Long managerId;

	// Added 28022024 - Koh He Xiang
	private String location;

	private String country;

	private String designation;

	private List<Long> groups;
}
