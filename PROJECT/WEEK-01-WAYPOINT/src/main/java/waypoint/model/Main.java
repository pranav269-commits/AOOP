package waypoint.model;

import java.time.Duration;
import java.time.Instant;

/**
 * MAIN -- runs / demonstrates every file in 7.2 in one place:
 *   Money, Currency, Registration, Ids (TicketId / SpotId / VehicleId),
 *   Spot, Ticket, FarePolicy, TicketAlreadyClosed.
 *
 * Run it with:
 *   javac -d out $(find src/main/java -name "*.java")
 *   java  -cp out waypoint.model.Main
 * or with Maven:  mvn -q compile exec:java
 *
 * Every demonstration is a check: it prints PASS/FAIL, and the program
 * exits non-zero if any check fails.
 */
public class Main {

    private static int passed = 0;
    private static int total = 0;

    private static void check(String label, boolean condition) {
        total++;
        if (condition) {
            passed++;
            System.out.println("  PASS  " + label);
        } else {
            System.out.println("  FAIL  " + label);
        }
    }

    private static void checkThrows(String label, Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
            check(label + " (expected " + type.getSimpleName() + ", nothing was thrown)", false);
        } catch (Throwable t) {
            check(label + " -> throws " + type.getSimpleName(), type.isInstance(t));
        }
    }

    public static void main(String[] args) {
        Instant t10 = Instant.parse("2026-03-01T10:00:00Z");
        Instant t09 = Instant.parse("2026-03-01T09:00:00Z");
        Instant t12 = Instant.parse("2026-03-01T12:00:00Z");
        Instant t1230 = Instant.parse("2026-03-01T12:30:00Z");

        // ---------- 1. Money.java + Currency.java ----------
        System.out.println("== Money ==");
        System.out.println("  Money.rupees(120)            = " + Money.rupees(120) + "  (minor=" + Money.rupees(120).minor() + ")");
        System.out.println("  Rs 1 + 25 paise              = " + Money.rupees(1).plus(Money.ofMinor(25, Currency.INR)));
        System.out.println("  Rs 1 - 25 paise              = " + Money.rupees(1).minus(Money.ofMinor(25, Currency.INR)));
        System.out.println("  Rs 120 x 2                   = " + Money.rupees(120).times(2));
        var shares = Money.rupees(100).allocate(3);
        System.out.println("  Rs 100 allocated in 3 shares = " + shares + "  (sum must be exactly Rs 100)");

        check("zeroIsZero: rupees(0) equals ofMinor(0, INR)", Money.rupees(0).equals(Money.ofMinor(0, Currency.INR)));
        check("rupees(120).minor() == 12000", Money.rupees(120).minor() == 12000);
        check("plus keeps exact paise", Money.rupees(1).plus(Money.ofMinor(25, Currency.INR)).equals(Money.ofMinor(125, Currency.INR)));
        check("minus keeps exact paise", Money.rupees(1).minus(Money.ofMinor(25, Currency.INR)).equals(Money.ofMinor(75, Currency.INR)));
        check("times scales", Money.rupees(120).times(2).equals(Money.rupees(240)));
        check("equal values are equal + same hashCode",
                Money.rupees(120).equals(Money.ofMinor(12000, Currency.INR))
                        && Money.rupees(120).hashCode() == Money.ofMinor(12000, Currency.INR).hashCode());
        check("allocate(3) returns 3 shares summing exactly to Rs 100",
                shares.size() == 3 && shares.get(0).plus(shares.get(1)).plus(shares.get(2)).equals(Money.rupees(100)));
        check("isNegative / compareTo work",
                Money.ofMinor(-50, Currency.INR).isNegative()
                        && Money.rupees(1).compareTo(Money.rupees(2)) < 0
                        && Money.rupees(2).compareTo(Money.rupees(2)) == 0);
        checkThrows("mixing INR + USD", IllegalArgumentException.class,
                () -> Money.rupees(1).plus(Money.ofMinor(100, Currency.USD)));
        checkThrows("allocate(0)", IllegalArgumentException.class, () -> Money.rupees(10).allocate(0));

        // ---------- 2. Registration.java ----------
        System.out.println("\n== Registration ==");
        Registration reg = new Registration("  mh12ab1234  ");
        System.out.println("  new Registration(\"  mh12ab1234  \") -> stored as \"" + reg.value() + "\" (trimmed + upper-cased)");
        check("registration is normalised to MH12AB1234", reg.value().equals("MH12AB1234"));
        check("one-letter series also valid (MH12A1234)", new Registration("mh12a1234").value().equals("MH12A1234"));
        check("equal registrations are equal", new Registration("MH12AB1234").equals(reg));
        checkThrows("invalid registration 'MH1234'", IllegalArgumentException.class, () -> new Registration("MH1234"));
        checkThrows("invalid registration '1234AB5678'", IllegalArgumentException.class, () -> new Registration("1234AB5678"));
        checkThrows("null registration", NullPointerException.class, () -> new Registration(null));

        // ---------- 3. Ids.java ----------
        System.out.println("\n== Ids (TicketId / SpotId / VehicleId) ==");
        System.out.println("  TicketId(\"T1\")=" + new TicketId("T1") + ", SpotId(\"S1\")=" + new SpotId("S1") + ", VehicleId(\"V1\")=" + new VehicleId("V1"));
        System.out.println("  TicketId and SpotId are different types -- passing one where the other is expected does not compile.");
        check("TicketId trims its value", new TicketId("  T1  ").value().equals("T1"));
        checkThrows("blank TicketId", IllegalArgumentException.class, () -> new TicketId("   "));
        checkThrows("blank SpotId", IllegalArgumentException.class, () -> new SpotId(""));
        checkThrows("null VehicleId", NullPointerException.class, () -> new VehicleId(null));

        // ---------- 4. Spot.java ----------
        System.out.println("\n== Spot ==");
        Spot spot = new Spot(new SpotId("S1"));
        System.out.println("  New spot: " + spot);
        check("a new spot is free", spot.isFree());
        check("first tryAssign(V1) succeeds", spot.tryAssign(new VehicleId("V1")));
        System.out.println("  After assign: " + spot);
        check("second tryAssign(V2) fails, does not overwrite", !spot.tryAssign(new VehicleId("V2"))
                && spot.occupant().orElseThrow().equals(new VehicleId("V1")));
        spot.release(new VehicleId("V2"));
        check("release by a non-occupant changes nothing", !spot.isFree());
        spot.release(new VehicleId("V1"));
        check("release by the occupant frees the spot", spot.isFree());
        spot.release(new VehicleId("V1"));
        check("release is idempotent -- releasing a free spot is harmless", spot.isFree());
        Spot sameId = new Spot(new SpotId("S1"));
        sameId.tryAssign(new VehicleId("V9"));
        check("spot identity is its id, not its state (equals + hashCode)",
                spot.equals(sameId) && spot.hashCode() == sameId.hashCode());

        // ---------- 5. Ticket.java + FarePolicy.java + TicketAlreadyClosed ----------
        System.out.println("\n== Ticket + FarePolicy ==");
        FarePolicy flat40 = (entry, exit, s) -> Money.rupees(40);
        // A second, real policy: Rs 20 per started hour, computed with Duration -- no floating-point types.
        FarePolicy hourly20 = (entry, exit, s) -> {
            long seconds = Duration.between(entry, exit).getSeconds();
            long startedHours = (seconds + 3599) / 3600;
            return Money.rupees(20 * startedHours);
        };

        Ticket ticket = Ticket.open(new TicketId("T1"), new SpotId("S1"), t10);
        System.out.println("  Opened: " + ticket);
        check("a new ticket is open, fare/exit are null", ticket.isOpen() && ticket.fare() == null && ticket.exit() == null);

        checkThrows("close with exit before entry (09:00 < 10:00)", IllegalArgumentException.class,
                () -> ticket.close(t09, flat40));
        check("a rejected close leaves the ticket untouched (still open)", ticket.isOpen());

        Money fare = ticket.close(t12, flat40);
        System.out.println("  Closed with FLAT policy: " + ticket);
        check("fare comes from the injected policy (Rs 40)", fare.equals(Money.rupees(40)) && ticket.fare().equals(Money.rupees(40)));
        check("a closed ticket is not open", !ticket.isOpen());
        checkThrows("closing twice", TicketAlreadyClosed.class, () -> ticket.close(t12, flat40));

        Ticket ticket2 = Ticket.open(new TicketId("T2"), new SpotId("S2"), t10);
        Money hourlyFare = ticket2.close(t1230, hourly20);
        System.out.println("  10:00 -> 12:30 with HOURLY Rs20/started-hour policy: fare = " + hourlyFare + " (3 started hours x Rs 20)");
        check("hourly policy: 2.5 hours = 3 started hours = Rs 60", hourlyFare.equals(Money.rupees(60)));

        // ---------- Full flow: Spot + Registration + Ticket together ----------
        System.out.println("\n== Full flow (all files together) ==");
        Spot bay = new Spot(new SpotId("S7"));
        Registration car = new Registration("mh12ab1234");
        VehicleId vehicle = new VehicleId(car.value());
        Ticket visit = Ticket.open(new TicketId("T7"), bay.id(), t10);
        boolean parked = bay.tryAssign(vehicle);
        Money paid = visit.close(t12, flat40);
        bay.release(vehicle);
        System.out.println("  " + car.value() + " parked in S7 at 10:00, left at 12:00, paid " + paid + ", spot free again: " + bay.isFree());
        check("full flow: park -> ticket -> pay Rs 40 -> release -> spot free", parked && paid.equals(Money.rupees(40)) && bay.isFree());

        // ---------- Summary ----------
        System.out.println("\n==========================================");
        System.out.println("Main demo: " + passed + "/" + total + " checks passed");
        if (passed != total) {
            System.out.println("SOME CHECKS FAILED");
            System.exit(1);
        }
        System.out.println("ALL CHECKS PASSED");
    }
}
