package waypoint.model;

import java.time.Instant;
import java.util.Objects;

/**
 * ENTITY. Invariants:
 *   I1  entry is always present
 *   I2  exit is null, or not before entry
 *   I3  fare is null, or not negative
 *   I4  fare exists only if exit exists
 *   I5  a closed ticket never reopens
 *
 * Exactly two fields are non-final: exit and fare. They are the only state
 * that genuinely changes, and they change exactly once, together, in close().
 * No setters. No clock calls in this file -- time arrives as a parameter.
 */
public final class Ticket {

    private final TicketId id;
    private final SpotId spotId;
    private final Instant entry;
    private Instant exit;   // non-final #1 -- set once by close()
    private Money fare;     // non-final #2 -- set once by close(), together with exit

    private Ticket(TicketId id, SpotId spotId, Instant entry) {
        this.id = Objects.requireNonNull(id, "ticket id must not be null");
        this.spotId = Objects.requireNonNull(spotId, "spot id must not be null");
        this.entry = Objects.requireNonNull(entry, "entry time must not be null");
        this.exit = null;
        this.fare = null;
    }

    /** Open a new ticket. This is the only way to create one. */
    public static Ticket open(TicketId id, SpotId spotId, Instant entry) {
        return new Ticket(id, spotId, entry);
    }

    public TicketId id() {
        return id;
    }

    public SpotId spotId() {
        return spotId;
    }

    public Instant entry() {
        return entry;
    }

    /** Null while the ticket is still open (I2/I4). */
    public Instant exit() {
        return exit;
    }

    /** Null while the ticket is still open (I3/I4). */
    public Money fare() {
        return fare;
    }

    public boolean isOpen() {
        return exit == null;
    }

    /**
     * Close the ticket exactly once and compute its fare from the injected policy.
     *
     * @throws TicketAlreadyClosed    if this ticket was already closed (I5)
     * @throws IllegalArgumentException if {@code at} is before entry (I2),
     *                                  or the policy returns a negative fare (I3)
     * A rejected close leaves the ticket completely untouched.
     */
    public Money close(Instant at, FarePolicy policy) {
        if (!isOpen()) {
            throw new TicketAlreadyClosed("Ticket " + id.value() + " is already closed");
        }
        Objects.requireNonNull(at, "exit time must not be null");
        Objects.requireNonNull(policy, "fare policy must not be null");
        if (at.isBefore(entry)) {
            throw new IllegalArgumentException("exit " + at + " is before entry " + entry);
        }
        Money computed = Objects.requireNonNull(
                policy.fareFor(entry, at, spotId), "fare policy returned null");
        if (computed.isNegative()) {
            throw new IllegalArgumentException("fare must not be negative: " + computed);
        }
        // All validation passed -- only now mutate, and set both together (I4).
        this.exit = at;
        this.fare = computed;
        return computed;
    }

    /** Entity identity: equals/hashCode on id alone. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ticket[id=" + id.value() + ", spot=" + spotId.value()
                + ", entry=" + entry + ", " + (isOpen() ? "open" : "closed at " + exit + ", fare=" + fare) + "]";
    }
}
