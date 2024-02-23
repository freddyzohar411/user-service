package com.avensys.rts.userservice.payload.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserListingResponseDTO {
	private Integer totalPages;
	private Long totalElements;
	private Integer page;
	private Integer pageSize;
	private List<UserResponseDTO> users;
}
