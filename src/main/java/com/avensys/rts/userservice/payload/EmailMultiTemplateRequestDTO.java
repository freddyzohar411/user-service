package com.avensys.rts.userservice.payload;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
