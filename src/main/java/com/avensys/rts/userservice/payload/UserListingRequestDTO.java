package com.avensys.rts.userservice.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserListingRequestDTO {
	private Integer page = 0;
	private Integer pageSize = 5;
	private String sortBy;
	private String sortDirection;
	private String searchTerm;
}
