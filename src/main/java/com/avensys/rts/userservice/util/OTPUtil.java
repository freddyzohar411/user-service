package com.avensys.rts.userservice.util;

import java.security.SecureRandom;

public class OTPUtil {

	/**
	 * Generate a random numberic OTP
	 * @param length
	 * @return
	 */
	public static String generateNumericOtp(int length) {

		SecureRandom random = new SecureRandom();
		StringBuilder otp = new StringBuilder();

		for (int i = 0; i < length; i++) {
			otp.append(random.nextInt(10)); // 10 is exclusive
		}
		return otp.toString();
	}

}
