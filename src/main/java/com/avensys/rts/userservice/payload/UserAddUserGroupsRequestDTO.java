package com.avensys.rts.userservice.payload;

import com.avensys.rts.userservice.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAddUserGroupsRequestDTO {
	private Long userId;
	private List<Long> userGroupIds;
}
