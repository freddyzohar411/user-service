package com.avensys.rts.userservice.payload;

import com.avensys.rts.userservice.payload.response.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListingRequestDTO {
	private Integer page = 0;
	private Integer pageSize = 5;
	private String sortBy;
	private String sortDirection;
	private String searchTerm;
}
