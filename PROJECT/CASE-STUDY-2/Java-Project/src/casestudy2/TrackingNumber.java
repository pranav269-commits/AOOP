package casestudy2;

/**
 * VALUE OBJECT - a carrier tracking number.
 * Validated once, at construction: a blank tracking number
 * can never exist, so ship() never has to re-check it.
 */
public record TrackingNumber(String value) {

    public TrackingNumber {
        value = Ids.requireText(value, "Tracking number");
    }

    public static TrackingNumber of(String value) {
        return new TrackingNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
