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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;

@Setter
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

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "role_modules", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "module_id", referencedColumnName = "id"))
	private Set<ModuleEntity> modules;

	@ManyToMany(mappedBy = "roleEntities")
	@JsonProperty(access = Access.WRITE_ONLY)
	private Set<UserGroupEntity> groupEntities;

	public Long getId() {
		return id;
	}

	public String getRoleName() {
		return roleName;
	}

	public String getRoleDescription() {
		return roleDescription;
	}

	public Set<ModuleEntity> getModules() {
		return modules;
	}

	public Set<UserGroupEntity> getGroupEntities() {
		return groupEntities;
	}

}