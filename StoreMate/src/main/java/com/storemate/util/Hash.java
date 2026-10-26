package com.storemate.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Hash SHA-256 (hex) - cocok dengan SHA2(x,256) di MySQL. */
public final class Hash {
    private Hash() { }

    public static String sha256(String s) {
        try {
            byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
