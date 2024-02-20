package com.avensys.rts.userservice.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgetResetPasswordRequestDTO {
	private String token;
	private String password;
	private String confirmPassword;
}
