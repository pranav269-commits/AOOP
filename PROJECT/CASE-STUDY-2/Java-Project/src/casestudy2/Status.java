package casestudy2;

/**
 * VALUE (enum) - the CLOSED set of order states (Model A).
 *
 * The naive design used a String, so a typo like "SHIPED" was a
 * perfectly storable "state". An enum admits exactly these six
 * values and nothing else - and DELIVERED / CANCELLED are terminal:
 * no transition method accepts them as a starting state.
 */
public enum Status {
    DRAFT,
    PLACED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
