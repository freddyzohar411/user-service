package com.avensys.rts.userservice.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Data
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
}
