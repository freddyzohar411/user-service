package com.avensys.rts.userservice.payload;

import com.avensys.rts.userservice.payload.response.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseListDTO {
	List<UserResponseDTO> users;
}
