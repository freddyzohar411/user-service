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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "roles", uniqueConstraints = { @UniqueConstraint(columnNames = { "role_name" }) })
public class RoleEntity extends BaseEntity {

	private static final long serialVersionUID = 2991860491601656766L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Role name cannot be empty")
	@Column(name = "role_name")
	private String roleName;

	@NotNull(message = "Role description cannot be empty")
	@Column(name = "role_description")
	private String roleDescription;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Set<RoleModulePermissionsEntity> modulePermissions;

	@ManyToMany(mappedBy = "roleEntities", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JsonProperty(access = Access.WRITE_ONLY)
	private Set<UserGroupEntity> groupEntities;

}