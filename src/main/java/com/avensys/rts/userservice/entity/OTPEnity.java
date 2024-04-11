package com.avensys.rts.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "otp")
@Table(name = "otp")
public class OTPEnity {
	private static final long serialVersionUID = -7990682468483269984L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "token")
	private String otpToken;

	@Column(name = "expiry_time")
	private LocalDateTime expiryTime;

	@Column(name = "is_used")
	private boolean isUsed = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
}
