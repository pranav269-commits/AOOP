package casestudy5;

import java.util.Objects;
import java.util.UUID;

/**
 * VALUE OBJECT - a typed wrapper around the account's identity.
 * A UserId can never be accidentally passed where an EmailAddress or a
 * VerificationToken is expected, and two UserIds are equal iff the
 * underlying UUIDs are equal.
 */
public record UserId(UUID value) {
    public UserId {
        Objects.requireNonNull(value, "UserId value must not be null");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
