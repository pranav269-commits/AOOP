package waypoint.model;

import java.util.Objects;
import java.util.Optional;

/**
 * ENTITY. Identity = id.
 * Invariant: a spot cannot be assigned while occupied.
 */
public final class Spot {

    private final SpotId id;
    private VehicleId occupant;    // the ONLY mutable field

    public Spot(SpotId id) {
        this.id = Objects.requireNonNull(id, "spot id must not be null");
    }

    public SpotId id() {
        return id;
    }

    /** The current occupant, or empty if the spot is free. */
    public Optional<VehicleId> occupant() {
        return Optional.ofNullable(occupant);
    }

    /**
     * Try to take this spot.
     * @return true only if THIS call took it; false if it was already occupied
     *         (the existing occupant is never overwritten).
     */
    public boolean tryAssign(VehicleId v) {
        Objects.requireNonNull(v, "vehicle id must not be null");
        if (occupant != null) {
            return false;
        }
        occupant = v;
        return true;
    }

    /**
     * Release this spot. Idempotent: releasing a free spot is not an error,
     * and releasing with a vehicle that is not the occupant changes nothing.
     */
    public void release(VehicleId v) {
        if (occupant != null && occupant.equals(v)) {
            occupant = null;
        }
    }

    public boolean isFree() {
        return occupant == null;
    }

    /** Entity identity: equals/hashCode on id ALONE, never on state. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Spot other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Spot[id=" + id.value() + ", " + (isFree() ? "free" : "occupied by " + occupant.value()) + "]";
    }
}
