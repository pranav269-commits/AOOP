package waypoint.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * VALUE OBJECT. A vehicle registration, validated and normalised at
 * construction, so an invalid instance cannot exist.
 *
 * Format: two letters, two digits, one or two letters, four digits.
 * Example: MH12AB1234 (also MH12A1234)
 */
public record Registration(String value) {

    private static final Pattern FORMAT =
            Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$");

    public Registration {
        Objects.requireNonNull(value, "registration must not be null");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Invalid registration '" + value
                            + "' -- expected two letters, two digits, one or two letters, four digits, e.g. MH12AB1234");
        }
        // Reassigning the parameter in a compact constructor sets the field,
        // so the stored value is always the normalised one.
    }
}
