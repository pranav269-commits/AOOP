# AOOP (25CS2205E) — Week 01, Section 7.2 — Waypoint Core Model — COMPLETED

Source: https://y25btech.klef.in/slm/25CS2205E.html#/w01
Section: **7.2 · Starter files and the failing suite**
Instruction in 7.2: "Copy these into a project. The suite uses JUnit 5. Run it first and read the failures — that list is your specification."

All TODOs in the starter files are completed, the given JUnit 5 suite passes,
and a `Main.java` has been created that runs/demonstrates every file.

## Files

### Given in 7.2 — completed
| File | What was TODO / done |
|---|---|
| `Money.java` | Fields (long paise + Currency, both final), private constructor, `ofMinor` / `rupees` factories, `plus`, `minus`, `times(int)`, `isNegative`, `compareTo`, `allocate(int)` (shares sum EXACTLY), `equals` / `hashCode` / `toString`, plus a `minor()` accessor the tests need. No double/float anywhere. Mixing currencies throws `IllegalArgumentException`. |
| `Registration.java` | Compact constructor: `requireNonNull`, trim, upper-case, then validate against `^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$` (e.g. MH12AB1234). An invalid instance cannot be created. |
| `Ids.java` | `TicketId` and `SpotId` records reject null/blank (and trim). See note 1 below. |
| `Spot.java` | Validating constructor, `tryAssign` (true only if this call took it), `release` (idempotent), `isFree`, `equals`/`hashCode` on id alone. Only `occupant` is mutable. |
| `Ticket.java` | Fields — exactly two non-final (`exit`, `fare`), private constructor + `open(...)` factory, `isOpen()`, `close(at, policy)` which rejects a second close with `TicketAlreadyClosed`, rejects exit-before-entry with `IllegalArgumentException`, validates the fare is not negative, and only then sets exit and fare together (a rejected close leaves the ticket untouched). No setters, no `Instant.now()`. |
| `FarePolicy.java` | No TODOs — kept exactly as supplied. |
| `MoneyTest.java` / `TicketTest.java` / `SpotTest.java` | Given tests, unchanged except a `package waypoint.model;` line was added so they compile in the project layout (the page shows them with no package/imports). |

### Missing in 7.2 — had to be created, or nothing compiles
| File | Why |
|---|---|
| `Currency.java` | MoneyTest writes `Currency.INR` / `Currency.USD`. `java.util.Currency` has no such constants, so a `Currency` enum (INR, USD, EUR, GBP, JPY, AUD, CAD) is defined in `waypoint.model`. |
| `VehicleId` (inside `Ids.java`) | Used by `Spot.java` and `SpotTest.java`, but not supplied in 7.2. Added alongside TicketId/SpotId with the same null/blank validation. |
| `TicketAlreadyClosed.java` | The exception asserted in `TicketTest.closingTwiceIsRejected`. Not supplied in 7.2. Defined as a `RuntimeException`. |
| `Main.java` | **New, as requested.** Runs/demonstrates all files above (Money, Registration, Ids, Spot, Ticket, both FarePolicies, full park→pay→release flow) as printed PASS/FAIL checks. |

### Note 1 — Ids.java visibility
Java allows only ONE public top-level type per file, so the three records in
`Ids.java` cannot all be `public` in one file — the starter as literally
written does not compile. They are package-private in `Ids.java` (everything
in `waypoint.model` — model, tests and Main — uses them directly). This keeps
the original file name and grouping, and the distinct-type guarantee is
unchanged: a `TicketId` still cannot be passed where a `SpotId` is expected.

## Project layout
```
waypoint-w01/
  pom.xml
  README.md
  output.txt                     <- captured run of Main + tests
  src/main/java/waypoint/model/
    Main.java  Money.java  Currency.java  Registration.java  Ids.java
    Spot.java  Ticket.java  FarePolicy.java  TicketAlreadyClosed.java
  src/test/java/waypoint/model/
    MoneyTest.java  TicketTest.java  SpotTest.java
```

## How to run

With Maven (Java 17+):
```
mvn test                        # runs MoneyTest + TicketTest + SpotTest (15 tests)
mvn -q compile exec:java        # runs Main
```

Without Maven (Main only, no JUnit needed):
```
javac -d out $(find src/main/java -name "*.java")     # Windows PowerShell: javac -d out (Get-ChildItem -Recurse src/main/java -Filter *.java).FullName
java -cp out waypoint.model.Main
```

Constraints from 7.1, all honoured: zero public setters; no double/float
anywhere; every field final unless the domain genuinely mutates it (Spot:
only `occupant`; Ticket: exactly `exit` + `fare`); no `Instant.now()` inside
a domain class — time always arrives as a parameter.
