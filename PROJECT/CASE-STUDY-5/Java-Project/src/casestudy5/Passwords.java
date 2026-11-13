package casestudy5;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password hashing helper for the demo.
 *
 * DEMO ONLY: SHA-256 is used so the project has zero dependencies.
 * A real system must use a slow, salted password hash
 * (BCrypt / Argon2 / PBKDF2) - never a fast general-purpose hash,
 * and never the plain password.
 */
final class Passwords {

    private Passwords() {
    }

    static String hash(String password) {
        if (password == null || password.length() < 8) {
            throw new DomainException("Password must be at least 8 characters");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
