package casestudy2;

import java.util.Objects;
import java.util.UUID;

/**
 * VALUE OBJECTS - typed identity wrappers.
 * An OrderId can never be passed where a ProductId belongs,
 * and ids of different kinds can never be swapped by accident.
 */
final class Ids {
    private Ids() {
    }

    static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(field + " must not be blank");
        }
        return value.trim();
    }
}

/** An order's system-generated identity. */
record OrderId(UUID value) {
    OrderId {
        Objects.requireNonNull(value, "OrderId");
    }

    static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }
}

/** A product's identity is its SKU / catalogue code, e.g. "P-100". */
record ProductId(String value) {
    ProductId {
        value = Ids.requireText(value, "Product id");
    }

    static ProductId of(String value) {
        return new ProductId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

/** A customer's system-generated identity. */
record CustomerId(UUID value) {
    CustomerId {
        Objects.requireNonNull(value, "CustomerId");
    }

    static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }
}
