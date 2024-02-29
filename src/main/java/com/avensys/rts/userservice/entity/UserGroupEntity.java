package com.avensys.rts.userservice.entity;

import java.util.Set;

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
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "user_group", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_group_name" }) })
public class UserGroupEntity extends BaseEntity {

	private static final long serialVersionUID = -19569507092908100L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull(message = "User group name cannot be empty")
	@Column(name = "user_group_name")
	private String userGroupName;

	@NotNull(message = "User group description cannot be empty")
	@Column(name = "user_group_description")
	private String userGroupDescription;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "user_group_intersaction", joinColumns = @JoinColumn(name = "user_group_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
	private Set<UserEntity> users;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "user_group_roles", joinColumns = @JoinColumn(name = "user_group_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
	private Set<RoleEntity> roleEntities;

	public void addUser(UserEntity userEntity) {
		if (this.users == null) {
			this.users = new java.util.HashSet<>();
		}
		this.users.add(userEntity);
	}

}