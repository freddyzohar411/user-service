package com.avensys.rts.userservice.payload;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailMultiTemplateRequestDTO {
	private String[] to;
	private String[] Bcc;
	private String[] Cc;
	private String subject;
	private String content;
	private String category;
	private String subCategory;
	private String templateName;
	Map<String, String> templateMap;
}
