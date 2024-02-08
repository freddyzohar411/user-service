package com.avensys.rts.userservice.util;

import java.util.Base64;

public class PasswordUtil {

	public static String decode(String encryptedString) {
		String originalString = encryptedString.substring(0, 4).concat(encryptedString.substring(8));
		byte[] decodedBytes = Base64.getDecoder().decode(originalString);
		String password = new String(decodedBytes);
		decodedBytes = Base64.getDecoder().decode(password);
		password = new String(decodedBytes);
		return password;
	}

}
