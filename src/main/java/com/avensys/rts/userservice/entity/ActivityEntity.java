package com.avensys.rts.userservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity")
public class ActivityEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String module;
	private String subModule;
	private Long moduleId;
	private String action;
	private Long salesId;
	private Long recruiterId;
	private Long candidateId;

	@ManyToOne(fetch = FetchType.LAZY)
	private UserEntity user;

	@JdbcTypeCode(SqlTypes.JSON)
	private JsonNode auditData;
}
