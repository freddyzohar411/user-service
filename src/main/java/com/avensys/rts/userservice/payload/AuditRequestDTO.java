package com.avensys.rts.userservice.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditRequestDTO {
	private String module;
	private String subModule;
	private Long moduleId;
	private String action;
	private Long salesId;
	private Long recruiterId;
	private Long candidateId;
	private JsonNode auditData;
}
