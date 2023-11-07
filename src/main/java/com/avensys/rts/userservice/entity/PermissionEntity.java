package com.avensys.rts.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

}
