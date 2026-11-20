package waypoint.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * VALUE OBJECT. Exact money.
 * Immutable, currency-aware, stored as minor units (paise) in a long.
 * No floating-point types anywhere in this file.
 */
public final class Money implements Comparable<Money> {

    private final long minor;
    private final Currency currency;

    private Money(long minor, Currency currency) {
        this.minor = minor;
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
    }

    /** Factory: amount already in minor units, e.g. ofMinor(125, INR) = Rs 1.25 */
    public static Money ofMinor(long minor, Currency currency) {
        return new Money(minor, currency);
    }

    /** Factory: whole rupees, always INR. rupees(120) = 12000 paise. */
    public static Money rupees(long rupees) {
        return new Money(Math.multiplyExact(rupees, 100L), Currency.INR);
    }

    public long minor() {
        return minor;
    }

    public Currency currency() {
        return currency;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    "Cannot mix currencies: " + this.currency + " and " + other.currency);
        }
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(this.minor, other.minor), this.currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.subtractExact(this.minor, other.minor), this.currency);
    }

    public Money times(int factor) {
        return new Money(Math.multiplyExact(this.minor, (long) factor), this.currency);
    }

    public boolean isNegative() {
        return minor < 0;
    }

    public boolean isZero() {
        return minor == 0;
    }

    /**
     * Split into n shares whose sum is EXACTLY this amount.
     * The remainder (minor % n) is spread one paise at a time over the
     * first shares, so no paise is created or lost. Works for negative
     * amounts too (remainder follows the sign of the amount).
     */
    public List<Money> allocate(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("number of shares must be positive: " + n);
        }
        long base = minor / n;
        long remainder = minor % n;              // sign matches minor, |remainder| < n
        long step = Long.signum(remainder);      // +1, 0 or -1 paise
        long extraShares = Math.abs(remainder);

        List<Money> parts = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            long share = base + (i < extraShares ? step : 0L);
            parts.add(new Money(share, currency));
        }
        return List.copyOf(parts);
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return Long.compare(this.minor, other.minor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return minor == other.minor && currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minor, currency);
    }

    /** e.g. "INR 120.00", "INR 1.25", "-USD 5.00" -- built with long arithmetic only. */
    @Override
    public String toString() {
        long abs = Math.abs(minor);
        long major = abs / 100;
        long fraction = abs % 100;
        String sign = minor < 0 ? "-" : "";
        return sign + currency + " " + major + "." + (fraction < 10 ? "0" : "") + fraction;
    }
}
