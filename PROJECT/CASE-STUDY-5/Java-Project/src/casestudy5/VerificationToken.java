package casestudy5;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * VALUE OBJECT - an email-verification token.
 *
 * The token carries everything needed to judge it INSIDE itself:
 * the secret, the email it was issued for, and its expiry instant.
 * A token therefore can never exist "without an expiry", and the
 * expiry check lives in isValidFor(...) - the only way a token is
 * ever used - so no caller can forget to check it.
 *
 * Production note (from Q4): the database should store only a HASH
 * of the secret, exactly like a password. The raw secret exists
 * only in the verification email/link.
 */
public record VerificationToken(String secret, EmailAddress issuedFor, Instant expiresAt) {

    private static final SecureRandom RANDOM = new SecureRandom();
    public static final Duration DEFAULT_TTL = Duration.ofHours(24);

    public VerificationToken {
        if (secret == null || secret.length() < 32) {
            throw new DomainException("Verification token secret is too weak");
        }
        Objects.requireNonNull(issuedFor, "issuedFor");
        Objects.requireNonNull(expiresAt, "expiresAt");
    }

    /** Generates a fresh, cryptographically random token for the given email. */
    public static VerificationToken generate(EmailAddress issuedFor, Instant now, Duration ttl) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new VerificationToken(secret, issuedFor, now.plus(ttl));
    }

    /**
     * THE validation - all four conditions, every time:
     * right account/email, right secret (constant-time comparison),
     * and not expired.
     */
    public boolean isValidFor(EmailAddress email, String presentedSecret, Instant now) {
        if (presentedSecret == null) {
            return false;
        }
        boolean sameSecret = MessageDigest.isEqual(
                secret.getBytes(StandardCharsets.UTF_8),
                presentedSecret.getBytes(StandardCharsets.UTF_8));
        return issuedFor.equals(email) && sameSecret && now.isBefore(expiresAt);
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
