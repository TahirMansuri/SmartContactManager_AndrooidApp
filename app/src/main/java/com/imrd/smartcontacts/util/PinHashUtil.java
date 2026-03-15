package com.imrd.smartcontacts.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PinHashUtil.java
 * -------------------------------------------------
 * Utility to hash a PIN using SHA-256 before
 * storing it in the database.
 *
 * We never store the raw PIN — only its hash.
 * On login, we hash the entered PIN and compare
 * it against the stored hash.
 * -------------------------------------------------
 */
public class PinHashUtil {

    private PinHashUtil() {} // utility class — no instances

    /**
     * Returns the SHA-256 hex string of the given PIN.
     * Returns null if hashing fails (should never happen).
     */
    public static String hash(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(pin.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns true if the given plain PIN matches the stored hash.
     */
    public static boolean verify(String plainPin, String storedHash) {
        if (plainPin == null || storedHash == null) return false;
        String hashed = hash(plainPin);
        return storedHash.equals(hashed);
    }
}
