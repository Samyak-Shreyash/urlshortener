package com.systemdesign.urlshortener.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class UrlUtils {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int SHORT_CODE_LENGTH = 6;
    
    public static String hashUrl(String url)
    {
        return generateSHA256Hash(url);
    }

    public static String generateFragmentHash(String fragment) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(fragment.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash).substring(0, 8); // Short hash
    } catch (NoSuchAlgorithmException e) {
        return Integer.toHexString(fragment.hashCode());
    }
}

    private static String generateSHA256Hash(String input) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
    }
}

    private static String bytesToHex(byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = Character.forDigit(v >>> 4, 16);
            hexChars[i * 2 + 1] = Character.forDigit(v & 0xF, 16);
        }
        return new String(hexChars);
    }

    
    public static synchronized String generateShortCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder shortCode = new StringBuilder(SHORT_CODE_LENGTH);

        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            shortCode.append(ALPHABET.charAt(randomIndex));
        }

        return shortCode.toString();
    }

    
}
