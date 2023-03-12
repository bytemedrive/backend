package com.bytemedrive.backend.privacy.control;

import javax.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@ApplicationScoped
public class ShaService {

    public String hashSha3(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        try {
            var digest = MessageDigest.getInstance("SHA3-256");
            var hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Cannot hash the text with SHA3-256", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
