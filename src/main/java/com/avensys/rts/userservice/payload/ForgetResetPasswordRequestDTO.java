package com.avensys.rts.userservice.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ForgetResetPasswordRequestDTO {
	private String token;
	private String password;
	private String confirmPassword;
}
