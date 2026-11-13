package casestudy2;

import java.time.Instant;
import java.util.Objects;

/**
 * VALUE OBJECT - the evidence that an order has shipped:
 * the tracking number AND the moment of shipment, together.
 *
 * This is what makes Invariant 4 hold by construction: the Order
 * holds an Optional<Shipment> that is empty until ship() runs and
 * present from that moment on - tracking/shippedAt exist if and
 * only if the order has shipped, in both directions.
 */
public record Shipment(TrackingNumber tracking, Instant shippedAt) {

    public Shipment {
        Objects.requireNonNull(tracking, "tracking");
        Objects.requireNonNull(shippedAt, "shippedAt");
    }
}
