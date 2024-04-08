package com.avensys.rts.userservice.payload;

import com.avensys.rts.userservice.payload.response.UserResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
	private String access_token;
	private String refresh_token;
	private String expires_in;
	private String refresh_expires_in;
	private String token_type;
	private UserResponseDTO user;
}
