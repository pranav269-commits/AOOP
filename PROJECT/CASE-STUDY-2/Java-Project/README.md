# CO-1 Case Study 2 — Demo Application (Word-doc match edition)

**AOOP (25CS2205E) · Case Study 2: Order Lifecycle and State**

This edition is rebuilt so the code matches the **original Word doc**
(`CO-1-Case-Study-2.docx`) **Refined Model A** skeleton as closely as
possible — same class names, field names, method names and signatures
as the doc snippet. **Model A** is the model the written submission
recommends in Q3 (one `Order` entity, one `Status` enum, guarded
transitions). Model B (one type per state) is comparison-only in the
written answer and is **not** implemented here.

- Requires **Java 17 or above**. No libraries, no build tool.
- Ready to open and run in **VS Code** (config included in `.vscode/`), and also runs
  from a plain terminal / Eclipse.
- Storage is in-memory, for classroom demonstration. The written answer (Q5) explains
  the matching database design — a versioned update / row lock that makes the
  credit check-and-reserve atomic in a real database.

## Word-doc Model A skeleton → this project

| Word-doc skeleton element / signature | File in this project |
|---|---|
| `final class Order { private final OrderId id; ... }` — entity / aggregate root | `src/casestudy2/Order.java` |
| `private Status status = Status.DRAFT;` | `Order.java` — same field, same initial value |
| `private final Map<ProductId, OrderLine> lines = new LinkedHashMap<>();` | `Order.java` — same field, same type, same initialiser |
| `private Optional<Shipment> shipment = Optional.empty();` | `Order.java` — set only inside `ship(...)` |
| `void addLine(ProductId p, Money unitPrice, Quantity q) { requireStatus(Status.DRAFT); lines.merge(p, new OrderLine(p, unitPrice, q), OrderLine::combine); }` | `Order.java` — `addLine(ProductId, Money, Quantity)`, body in the doc's form (plus null/currency checks) |
| `void place() { requireStatus(Status.DRAFT); if (lines.isEmpty()) throw new DomainException("cannot place an empty order"); status = Status.PLACED; }` | `Order.java` — `place()`, verbatim |
| `void ship(TrackingNumber t, Instant at) { requireStatus(Status.PAID); shipment = Optional.of(new Shipment(t, at)); status = Status.SHIPPED; }` | `Order.java` — `ship(TrackingNumber, Instant)`, verbatim in form |
| `Money total() { return lines.values().stream().map(OrderLine::total).reduce(Money.zero(currency), Money::plus); }` | `Order.java` — `total()`, verbatim; `Order` therefore also carries a `private final Currency currency` field, set via the constructor, to supply the `currency` the doc snippet references |
| `List<OrderLine> lines() { return List.copyOf(lines.values()); }` | `Order.java` — `lines()`, verbatim |
| Guarded transitions beyond the snippet: `pay()` PLACED→PAID, `deliver()` SHIPPED→DELIVERED, `cancel()` only from DRAFT/PLACED/PAID, `removeLine(...)` DRAFT only — all via the private `requireStatus(...)` guard | `Order.java` |
| Classification: `OrderLine` = Value (product ID, unit price, quantity), with a `total()` method and a `combine(OrderLine)` method usable as `OrderLine::combine` | `src/casestudy2/OrderLine.java` |
| Classification: `OrderId`, `ProductId`, `CustomerId` = separate value-object types wrapping IDs so they cannot be swapped — **separate files, not one combined file** | `src/casestudy2/OrderId.java`, `src/casestudy2/ProductId.java`, `src/casestudy2/CustomerId.java` |
| Classification: `TrackingNumber` = Value | `src/casestudy2/TrackingNumber.java` |
| Classification: `OrderStatus` = Value (closed set) — the Model A code names it `Status`, so the enum file is `Status.java` (this is mentioned in a comment in that file) with values DRAFT, PLACED, PAID, SHIPPED, DELIVERED, CANCELLED | `src/casestudy2/Status.java` |
| `Shipment` — value holding a `TrackingNumber` + `Instant shippedAt`, created only in `ship()` | `src/casestudy2/Shipment.java` |
| Case Study 1 model, reused by the doc: `enum Currency { EUR(2), USD(2), JPY(0), KWD(3); final int minorUnits; ... }` | `src/casestudy2/Currency.java` |
| Case Study 1 model: `final class Money { private final long minor; private final Currency currency; static Money of(String decimal, Currency c); Money plus(Money o) { requireSame(o); ... } Money times(Quantity q); static Money zero(Currency); private void requireSame(Money o) { ... throw new CurrencyMismatch(); } }` | `src/casestudy2/Money.java` (+ `src/casestudy2/CurrencyMismatch.java`) |
| Case Study 1 model: `final class Quantity { private final int value; /* >= 1 */ static Quantity of(int v) { if (v < 1) throw ...; } }` | `src/casestudy2/Quantity.java` |
| `DomainException` — thrown for an empty-order placement and every broken business rule | `src/casestudy2/DomainException.java` |
| Q5 cross-aggregate credit rule — **not** an Order invariant: a domain service reserves credit first, then places; the reservation is atomic | `src/casestudy2/CreditAccount.java` (aggregate, `synchronized` atomic `reserve(...)`) + `src/casestudy2/OrderPlacementService.java` (domain service) |
| Demo / presentation driver (10 scenarios, PASS/FAIL checks) | `src/casestudy2/Main.java` |

## What it demonstrates

`Main` runs 10 scenarios, **43 checks, all passing**. Each maps to a reported illegal state / question:

| # | Scenario | Design element |
|---|---|---|
| 1 | Empty order places; new order pays/ships/delivers | Guard on every transition; `place()` needs ≥ 1 line (Q1/Q2) |
| 2 | Duplicate product lines; total after adds/removes | Re-adding a product merges quantity via `OrderLine::combine`; `total()` derived (Q1/Q4) |
| 3 | Happy path DRAFT → PLACED → PAID → SHIPPED → DELIVERED | Guarded transitions; shipment exists iff shipped (Q2) |
| 4 | Cancelled order shipped anyway | `ship()`'s `requireStatus(PAID)` guard refuses it (Q2) |
| 5 | Shipped order cancelled; actions on terminal states | `cancel()` only from Draft/Placed/Paid; Delivered/Cancelled terminal (Q2) |
| 6 | Lines edited after placement | `addLine`/`removeLine` guarded to DRAFT only (Q1) |
| 7 | Shipping without payment; null/blank tracking number | `ship()` requires PAID + a validated `TrackingNumber` (Q2) |
| 8 | Stored total drifting from the lines | No stored total — `total()` recomputed from the lines (Q4) |
| 9 | Order placed over the customer's credit limit (limit 2,000.00 EUR, outstanding 500.00 EUR) | `OrderPlacementService` reserves on `CreditAccount` first (Q5) |
| 10 | Two orders race for the same credit at the same instant (two 1,500.00 EUR orders, 2,000.00 EUR limit) | Synchronized atomic `reserve(...)` — exactly one wins (Q5) |

The full sample run is saved in `output.txt`.

## Files

```
src/casestudy2/
  Main.java                 Demo/presentation driver (10 scenarios, PASS/FAIL)
  Order.java                ENTITY / aggregate root - Model A, exactly the doc skeleton; guarded transitions live here
  Status.java               ENUM - the closed set of states (doc Classification name: OrderStatus): DRAFT..CANCELLED
  OrderLine.java            VALUE OBJECT - product + unit price + Quantity; total() derived; combine() for merging
  Shipment.java             VALUE OBJECT - TrackingNumber + shippedAt (exists iff shipped)
  TrackingNumber.java       VALUE OBJECT - validated, never blank
  OrderId.java              VALUE OBJECT - an order's identity (own file)
  ProductId.java            VALUE OBJECT - a product's SKU identity (own file)
  CustomerId.java           VALUE OBJECT - a customer's identity (own file)
  Money.java                VALUE OBJECT - exact minor-unit amount (Case Study 1 model): long minor + Currency, Money.of(String, Currency)
  Currency.java             VALUE OBJECT (enum) - EUR/USD (2), JPY (0), KWD (3) minorUnits
  Quantity.java             VALUE OBJECT - positive int only, Quantity.of(int)
  CurrencyMismatch.java     Thrown by Money.requireSame on mixed currencies
  DomainException.java      Thrown whenever a business rule is broken
  CreditAccount.java        AGGREGATE - limit/outstanding/reserved; atomic reserve()
  OrderPlacementService.java  DOMAIN SERVICE - coordinates Order + CreditAccount (Q5)
.vscode/
  settings.json             Tells the VS Code Java extension where sources/output are
  launch.json               "Run Case Study 2 Demo" run configuration
```

## How to run — VS Code

1. Unzip the project, then in VS Code: **File → Open Folder** → select `case-study-2-docmatch`.
2. Make sure the **Extension Pack for Java** (Microsoft) is installed — VS Code usually
   prompts for it the moment it sees `.java` files. Let the Java language server finish
   importing (watch the bottom-right corner).
3. Open `src/casestudy2/Main.java` and either:
   - click **Run** above the `main` method, or
   - press **F5** and pick **Run Case Study 2 Demo**, or
   - use the integrated terminal (see below).

You should see `RESULT: 43 passed, 0 failed`.

### Or from the integrated terminal in VS Code (PowerShell)

Open the folder in VS Code, open the terminal with Ctrl + ` , then run, in **PowerShell**:

```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out casestudy2.Main
```

### Or from Command Prompt (CMD) on Windows

Open the project folder, click the address bar, type `cmd`, press Enter, then run, in **CMD**:

```bat
javac -d out src\casestudy2\*.java
java -cp out casestudy2.Main
```

## Presentation walkthrough (2 minutes)

1. Run `Main` — point at the final line: **43 passed, 0 failed**.
2. Open `Order.java`, method `ship(...)` — it is the Word doc's own code: one guard,
   `requireStatus(Status.PAID)`, is the whole reason a cancelled order can never be
   shipped: a cancelled order is in state CANCELLED, so the guard throws before
   anything is recorded (Scenario 4).
3. Open `Status.java` — six enum values are the *only* states that exist. There is
   no String to typo and no `paid`/`shipped`/`cancelled` flags to contradict
   each other (Scenario 1).
4. Point at `total()` in `Order.java` — the doc's one-liner: there is no total
   *field* at all. The total is summed from the lines on every call, so it
   cannot drift (Scenarios 2 and 8).
5. Close with `CreditAccount.reserve(...)` — the credit check and the reservation
   are one synchronized step, which is why the two-thread race in Scenario 10
   ends 1–1, never 2–0, and the 2,000.00 EUR limit is never breached (Scenarios 9–10).

## Honest scope

This is the "selected classes and methods" implementation the CO-1 submission asks
for (requirement 5) plus its demonstration (requirement 7) — a domain model with a
console demo, not a full production ordering system: no web UI, no database. Where
the production pieces plug in is covered in the written answer (Q3: persistence of
Model A as one status column; Q5: versioned update / per-account row lock for the
credit reservation) and noted in code comments. The demo implements Model A only;
Model B (one type per state) is compared in the written answer (Q3) but is not
implemented here — Model A is the recommended model for a lifecycle with many,
often-changing states.
