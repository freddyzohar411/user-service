package com.avensys.rts.userservice.payload;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
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
