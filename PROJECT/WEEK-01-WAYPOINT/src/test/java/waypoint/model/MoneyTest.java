package waypoint.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MoneyTest {
    @Test void zeroIsZero() { assertEquals(Money.rupees(0), Money.ofMinor(0, Currency.INR)); }
    @Test void rupeesIsMinorTimes100() { assertEquals(12000, Money.rupees(120).minor()); }
    @Test void plusKeepsExact() { assertEquals(Money.ofMinor(125, Currency.INR), Money.rupees(1).plus(Money.ofMinor(25, Currency.INR))); }
    @Test void minusKeepsExact() { assertEquals(Money.ofMinor(75, Currency.INR), Money.rupees(1).minus(Money.ofMinor(25, Currency.INR))); }
    @Test void mixingCurrenciesThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.rupees(1).plus(Money.ofMinor(100, Currency.USD)));
    }
    @Test void allocateSumsExactly() {
        var parts = Money.rupees(100).allocate(3);
        assertEquals(3, parts.size());
        assertEquals(Money.rupees(100), parts.get(0).plus(parts.get(1)).plus(parts.get(2)));
    }
    @Test void equalValuesAreEqual() {
        assertEquals(Money.rupees(120), Money.ofMinor(12000, Currency.INR));
        assertEquals(Money.rupees(120).hashCode(), Money.ofMinor(12000, Currency.INR).hashCode());
    }
    @Test void timesScales() { assertEquals(Money.rupees(240), Money.rupees(120).times(2)); }
}
