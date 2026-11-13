package casestudy5;

import java.util.Objects;

/**
 * VALUE OBJECT - a phone number stored in exactly ONE canonical form:
 * E.164 ("+" + country code + national number, digits only).
 *
 * "098765 43210", "+91 98765-43210" and "919876543210" (with default
 * country India) all parse to the same value +919876543210, and are
 * therefore equal - the multi-format storage bug cannot happen.
 */
public final class PhoneNumber {

    public static final String DEFAULT_COUNTRY_CODE = "91"; // India

    private final String e164;

    private PhoneNumber(String e164) {
        this.e164 = e164;
    }

    public static PhoneNumber parse(String raw) {
        return parse(raw, DEFAULT_COUNTRY_CODE);
    }

    public static PhoneNumber parse(String raw, String defaultCountryCode) {
        if (raw == null) {
            throw new DomainException("Phone number must not be null");
        }
        String s = raw.replaceAll("[\\s\\-().]", "");
        if (s.isEmpty()) {
            throw new DomainException("Phone number must not be empty");
        }

        if (s.startsWith("00")) {                       // international prefix
            s = "+" + s.substring(2);
        }
        if (!s.startsWith("+")) {
            String digits = s;
            if (!digits.chars().allMatch(Character::isDigit)) {
                throw new DomainException("Invalid phone number: '" + raw + "'");
            }
            if (digits.startsWith("0")) {               // trunk 0, e.g. 09876543210
                digits = digits.substring(1);
            }
            if (digits.length() == 10) {                // bare national number
                digits = defaultCountryCode + digits;
            }
            if (!digits.startsWith(defaultCountryCode)) {
                throw new DomainException(
                        "Cannot tell the country code of phone number: '" + raw + "'");
            }
            s = "+" + digits;
        }

        if (!s.substring(1).chars().allMatch(Character::isDigit)
                || !s.matches("\\+[1-9]\\d{7,14}")) {   // E.164 overall shape
            throw new DomainException("Invalid phone number: '" + raw + "'");
        }
        if (s.startsWith("+91")) {                      // India-specific shape
            String national = s.substring(3);
            if (national.length() != 10 || !national.matches("[6-9]\\d{9}")) {
                throw new DomainException("Invalid Indian phone number: '" + raw + "'");
            }
        }
        return new PhoneNumber(s);
    }

    public String value() {
        return e164;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PhoneNumber other && e164.equals(other.e164);
    }

    @Override
    public int hashCode() {
        return Objects.hash(e164);
    }

    @Override
    public String toString() {
        return e164;
    }
}
