package casestudy2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ENTITY and AGGREGATE ROOT - one order, MODEL A (Q3's recommendation):
 * a single entity whose lifecycle is one private Status field, changed
 * ONLY through guarded transition methods. There are no setters, the
 * lines list is private, and the total is DERIVED - never stored -
 * so none of the naive design's illegal states (Q1) can be built:
 *
 *   DRAFT --place()--> PLACED --pay()--> PAID --ship()--> SHIPPED
 *   SHIPPED --deliver()--> DELIVERED          (terminal)
 *   DRAFT / PLACED / PAID --cancel()--> CANCELLED   (terminal)
 *   Lines may be added/removed ONLY in DRAFT.
 *
 * Invariants held by construction:
 *   1. A placed (or later) order has at least one line - place()
 *      refuses an empty order, and lines lock at placement.
 *   2. total() always equals the sum of the line totals - it is
 *      computed from the lines on every call; no field can drift.
 *   3. Transitions follow the table above - every method guards.
 *   4. Shipment data exists IFF the order has shipped - the
 *      Optional<Shipment> is set inside ship(), together with the
 *      state change, and by no other method.
 *   5. Lines are unique by product - re-adding a product merges
 *      (increases) its quantity instead of adding a second line.
 */
public final class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private Status status;
    private final List<OrderLine> lines;
    private Optional<Shipment> shipment;

    public Order(OrderId id, CustomerId customerId) {
        this.id = Objects.requireNonNull(id, "order id");
        this.customerId = Objects.requireNonNull(customerId, "customer id");
        this.status = Status.DRAFT;
        this.lines = new ArrayList<>();
        this.shipment = Optional.empty();
    }

    public static Order draft(CustomerId customerId) {
        return new Order(OrderId.generate(), customerId);
    }

    // ------------------------------------------------------------------
    // Lines - DRAFT only; re-adding a product MERGES its quantity
    // ------------------------------------------------------------------

    public void addLine(ProductId product, Money unitPrice, int quantity) {
        requireStatus(Status.DRAFT, "add lines to");
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(unitPrice, "unit price");
        if (quantity <= 0) {
            throw new DomainException("Line quantity must be at least 1, got " + quantity);
        }
        for (int i = 0; i < lines.size(); i++) {
            OrderLine existing = lines.get(i);
            if (existing.product().equals(product)) {
                if (!existing.unitPrice().equals(unitPrice)) {
                    throw new DomainException("Product " + product
                            + " is already on this order at a different unit price");
                }
                // Invariant 5: same product -> ONE line, quantity grows
                lines.set(i, existing.withQuantity(existing.quantity() + quantity));
                return;
            }
        }
        lines.add(new OrderLine(product, unitPrice, quantity));
    }

    public void removeLine(ProductId product) {
        requireStatus(Status.DRAFT, "remove lines from");
        Objects.requireNonNull(product, "product");
        boolean removed = lines.removeIf(line -> line.product().equals(product));
        if (!removed) {
            throw new DomainException("Product " + product + " is not on this order");
        }
    }

    // ------------------------------------------------------------------
    // Transitions - the only doors through which state may change
    // ------------------------------------------------------------------

    public void place() {
        requireStatus(Status.DRAFT, "place");
        if (lines.isEmpty()) {
            throw new DomainException(
                    "Cannot place an empty order - a placed order needs at least one line");
        }
        this.status = Status.PLACED;
    }

    public void pay() {
        requireStatus(Status.PLACED, "pay");
        this.status = Status.PAID;
    }

    public void ship(TrackingNumber tracking, Instant at) {
        requireStatus(Status.PAID);                 // THE guard
        Objects.requireNonNull(tracking, "tracking");
        Objects.requireNonNull(at, "shippedAt");
        this.shipment = Optional.of(new Shipment(tracking, at));
        this.status = Status.SHIPPED;
    }

    public void deliver() {
        requireStatus(Status.SHIPPED, "deliver");
        this.status = Status.DELIVERED;
    }

    /** Cancellation is allowed ONLY from DRAFT, PLACED or PAID -
     *  never from SHIPPED, DELIVERED or CANCELLED. */
    public void cancel() {
        if (status == Status.SHIPPED || status == Status.DELIVERED
                || status == Status.CANCELLED) {
            throw new DomainException("Cannot cancel an order in state " + status
                    + " - only DRAFT, PLACED or PAID orders can be cancelled");
        }
        this.status = Status.CANCELLED;
    }

    // ------------------------------------------------------------------
    // The guard (Q2) - one helper, reused by every transition
    // ------------------------------------------------------------------

    private void requireStatus(Status expected) {
        if (status != expected) {
            throw new DomainException(
                "Cannot perform this action in state " + status
                + " (requires " + expected + ")");
        }
    }

    private void requireStatus(Status expected, String action) {
        if (status != expected) {
            throw new DomainException("Cannot " + action + " an order in state "
                    + status + " (requires " + expected + ")");
        }
    }

    // ------------------------------------------------------------------
    // Read-only access - NO setters, NO stored total
    // ------------------------------------------------------------------

    /** DERIVED (Q4): the sum of the line totals, computed fresh on
     *  every call from the single source of truth - the lines. */
    public Money total() {
        Money sum = Money.ZERO;
        for (OrderLine line : lines) {
            sum = sum.plus(line.lineTotal());
        }
        return sum;
    }

    public OrderId id() { return id; }
    public CustomerId customerId() { return customerId; }
    public Status status() { return status; }

    /** An unmodifiable VIEW - callers can read the lines but can
     *  never add/remove behind the aggregate's back (the naive
     *  design's public list is exactly what broke Invariant 2). */
    public List<OrderLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    public Optional<Shipment> shipment() { return shipment; }

    @Override
    public String toString() {
        return "Order[" + status + ", " + lines.size() + " line(s), total " + total() + "]";
    }
}
