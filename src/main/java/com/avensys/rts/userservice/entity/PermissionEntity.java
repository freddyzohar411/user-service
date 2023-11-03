package com.avensys.rts.userservice.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "permission", uniqueConstraints = { @UniqueConstraint(columnNames = { "permission_name" }) })
public class PermissionEntity extends BaseEntity {

	private static final long serialVersionUID = 281577856522059273L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "permission_name")
	private String permissionName;

	@Column(name = "permission_description")
	private String permissionDescription;

	@ManyToMany(mappedBy = "permissions", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JsonProperty(access = Access.WRITE_ONLY)
	private Set<ModuleEntity> moduleEntities;
}
