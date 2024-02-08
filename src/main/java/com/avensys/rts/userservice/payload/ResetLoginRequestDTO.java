package com.avensys.rts.userservice.payload;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetLoginRequestDTO {

	private Long userId;

	@NotNull(message = "Password cannot be empty")
	private String password;

	@NotNull(message = "Confirm password cannot be empty")
	private String confirmPassword;

}
