package com.avensys.rts.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "login_profile")
public class LoginHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "token")
	private String token;

	@Column(name = "token_creation_time")
	private String tokenCreationTime;

	@Column(name = "token_expiry_time")
	private String tokenExpiryTime;

	@Column(name = "mobile_no")
	private String mobileNo;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "os_type")
	private String osType;
	// (a,i,w)

	@Column(name = "os_version")
	private String osVersion;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "device_ip")
	private String deviceIP;

	@Column(name = "latitude")
	private String latitude;

	@Column(name = "logintude")
	private String logintude;

	@Column(name = "department")
	private String department;

	@Column(name = "app_version")
	private String appVersion;

}
