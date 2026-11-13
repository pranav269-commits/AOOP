package casestudy5;

import java.util.Locale;
import java.util.Objects;

/**
 * VALUE OBJECT - an email address that is valid and normalised BY CONSTRUCTION.
 *
 * The only way to create one is EmailAddress.of(...), which:
 *   1. trims surrounding whitespace,
 *   2. validates the format (exactly one '@', valid local part,
 *      domain with a dot and a real TLD, no spaces, length limits),
 *   3. lower-cases the whole address.
 *
 * Because equality and hashing use the normalised value,
 * "Asha@Example.COM" and "asha@example.com" are THE SAME EmailAddress -
 * which is what makes the uniqueness rule (Q5) enforceable.
 * Immutable: there are no setters; "changing" an email means building a new one.
 */
public final class EmailAddress {

    private final String normalised;

    private EmailAddress(String normalised) {
        this.normalised = normalised;
    }

    public static EmailAddress of(String raw) {
        if (raw == null) {
            throw new DomainException("Email address must not be null");
        }
        String email = raw.trim().toLowerCase(Locale.ROOT);

        if (email.isEmpty() || email.length() > 254) {
            throw new DomainException("Email address length is invalid: '" + raw + "'");
        }
        if (email.chars().anyMatch(Character::isWhitespace)) {
            throw new DomainException("Email address must not contain spaces: '" + raw + "'");
        }
        int at = email.indexOf('@');
        if (at < 0 || at != email.lastIndexOf('@')) {
            throw new DomainException("Email address must contain exactly one '@': '" + raw + "'");
        }
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        if (local.isEmpty() || local.length() > 64
                || !local.matches("[a-z0-9._%+\\-]+")
                || local.startsWith(".") || local.endsWith(".") || local.contains("..")) {
            throw new DomainException("Invalid local part of email address: '" + raw + "'");
        }
        if (!domain.contains(".")) {
            throw new DomainException("Email domain must contain a dot: '" + raw + "'");
        }
        String[] labels = domain.split("\\.");
        for (String label : labels) {
            if (!label.matches("[a-z0-9]([a-z0-9\\-]*[a-z0-9])?")) {
                throw new DomainException("Invalid domain in email address: '" + raw + "'");
            }
        }
        String tld = labels[labels.length - 1];
        if (tld.length() < 2 || !tld.chars().allMatch(Character::isLetter)) {
            throw new DomainException("Invalid top-level domain in email address: '" + raw + "'");
        }
        return new EmailAddress(email);
    }

    /** The normalised value - also the uniqueness key used by the repository. */
    public String value() {
        return normalised;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EmailAddress other && normalised.equals(other.normalised);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalised);
    }

    @Override
    public String toString() {
        return normalised;
    }
}
