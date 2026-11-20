package waypoint.model;

/**
 * Currency for {@link Money}.
 *
 * The test suite writes {@code Currency.INR} and {@code Currency.USD} as
 * constants, so this must be an enum with those constants.
 * {@code java.util.Currency} has no INR/USD fields (it is used as
 * {@code Currency.getInstance("INR")}), so it cannot satisfy MoneyTest.
 */
public enum Currency {
    INR,
    USD,
    EUR,
    GBP,
    JPY,
    AUD,
    CAD
}
