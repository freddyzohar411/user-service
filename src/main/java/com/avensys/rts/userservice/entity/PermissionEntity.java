package com.avensys.rts.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "permission", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class PermissionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(length = 60)
	private String name;

	@Column(length = 60)
	private String module;

	@Column(length = 60)
	private String action;

	@Column(name = "is_active")
	private boolean isActive;
}