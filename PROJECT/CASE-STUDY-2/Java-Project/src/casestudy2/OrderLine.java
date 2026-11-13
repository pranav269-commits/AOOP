package casestudy2;

import java.util.Objects;

/**
 * VALUE OBJECT - one line of an order: a product, the unit price
 * snapshot, and a quantity that is always at least 1.
 * Its total is DERIVED (unit price x quantity), never stored.
 */
public record OrderLine(ProductId product, Money unitPrice, int quantity) {

    public OrderLine {
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(unitPrice, "unit price");
        if (quantity <= 0) {
            throw new DomainException("Line quantity must be at least 1, got " + quantity);
        }
    }

    /** Derived, never stored. */
    public Money lineTotal() {
        return unitPrice.times(quantity);
    }

    /** A copy of this line with a different quantity (used when
     *  re-adding the same product MERGES quantities - Invariant 5). */
    public OrderLine withQuantity(int newQuantity) {
        return new OrderLine(product, unitPrice, newQuantity);
    }
}
