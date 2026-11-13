package casestudy2;

import java.util.Objects;

/**
 * VALUE OBJECT - an exact amount of money (Case Study 1's model).
 * Stored as an integer count of paise; never a double, never negative.
 * All amounts in this demo are INR.
 */
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    private final long paise;

    private Money(long paise) {
        if (paise < 0) {
            throw new DomainException("Money must not be negative");
        }
        this.paise = paise;
    }

    public static Money rupees(long rupees) {
        if (rupees < 0) {
            throw new DomainException("Money must not be negative");
        }
        return new Money(Math.multiplyExact(rupees, 100L));
    }

    public Money plus(Money other) {
        Objects.requireNonNull(other, "other amount");
        return new Money(Math.addExact(paise, other.paise));
    }

    /** Subtraction that REFUSES to go negative - a Money can never
     *  become a negative amount (Case Study 1's rule). */
    public Money minus(Money other) {
        Objects.requireNonNull(other, "other amount");
        if (other.paise > paise) {
            throw new DomainException("Money subtraction would go negative: "
                    + this + " - " + other);
        }
        return new Money(paise - other.paise);
    }

    public Money times(long factor) {
        if (factor < 0) {
            throw new DomainException("Money factor must not be negative");
        }
        return new Money(Math.multiplyExact(paise, factor));
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(paise, other.paise);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money other && paise == other.paise;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paise);
    }

    @Override
    public String toString() {
        return String.format("INR %d.%02d", paise / 100, paise % 100);
    }
}
