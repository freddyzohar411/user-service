package com.avensys.rts.userservice.payload;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMultiRequestDTO {
	private String[] to;
	private String[] Bcc;
	private String[] Cc;
	private String subject;
	private String content;
	private MultipartFile[] attachments;
}
