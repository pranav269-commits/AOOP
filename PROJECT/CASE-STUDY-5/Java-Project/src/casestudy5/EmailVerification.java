package casestudy5;

import java.time.Instant;
import java.util.Objects;

/**
 * Email verification as STATES, not flags (Case Study 2, Model B applied).
 *
 * Instead of: boolean emailVerified + nullable verifiedAt
 *             + nullable verificationToken + nullable tokenExpiry
 * (four fields, dozens of combinations, most of them contradictory),
 * there is ONE sealed type with exactly three states, and each state
 * carries exactly the data that state needs:
 *
 *   Unverified           - nothing has been issued yet
 *   PendingVerification  - a token is outstanding (it carries its own expiry)
 *   Verified             - carries the Instant it was verified at
 *
 * "Verified but no verification date" CANNOT be constructed:
 * a Verified value does not exist without its Instant.
 * "Verified but an old token is still live" cannot happen either:
 * becoming Verified REPLACES the PendingVerification, token and all.
 */
public sealed interface EmailVerification {

    record Unverified() implements EmailVerification {
    }

    record PendingVerification(VerificationToken token) implements EmailVerification {
        public PendingVerification {
            Objects.requireNonNull(token, "A pending verification must have a token");
        }
    }

    record Verified(Instant verifiedAt) implements EmailVerification {
        public Verified {
            Objects.requireNonNull(verifiedAt, "A verified email must have a verification date");
        }
    }
}
