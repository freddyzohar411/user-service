package com.avensys.rts.userservice.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMultiResponseDTO {
	private String message;
	private boolean status;
}
