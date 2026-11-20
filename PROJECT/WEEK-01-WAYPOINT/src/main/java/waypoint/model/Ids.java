package waypoint.model;

import java.util.Objects;

/**
 * Distinct id types so they can never be swapped at a call site.
 * A TicketId cannot be passed where a SpotId is expected -- the compiler stops it.
 *
 * Note on visibility: Java allows only ONE public top-level type per file,
 * so the three records in this one file are package-private (no 'public').
 * Everything in waypoint.model -- the model, the tests and Main -- uses them
 * directly. This is what the starter file intended; making all three
 * 'public' in one file does not compile.
 */
record TicketId(String value) {
    TicketId {
        Objects.requireNonNull(value, "TicketId must not be null");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("TicketId must not be blank");
        }
    }
}

record SpotId(String value) {
    SpotId {
        Objects.requireNonNull(value, "SpotId must not be null");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("SpotId must not be blank");
        }
    }
}

/**
 * Third id type -- referenced by Spot and SpotTest in 7.2 but not supplied
 * in the starter files, so it is defined here with the other ids.
 */
record VehicleId(String value) {
    VehicleId {
        Objects.requireNonNull(value, "VehicleId must not be null");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("VehicleId must not be blank");
        }
    }
}
