package waypoint.model;

import java.time.Instant;

/**
 * Strategy injected into Ticket.close() -- the ticket never hard-codes
 * how a fare is calculated. Complete as supplied in 7.2.
 */
@FunctionalInterface
public interface FarePolicy {
    Money fareFor(Instant entry, Instant exit, SpotId spot);
}
