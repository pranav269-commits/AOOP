# CO-1 Case Study 2 — Demo Application

**AOOP (25CS2205E) · Case Study 2: Order Lifecycle and State**

A small, dependency-free Java application that implements the domain model from the
Case Study 2 written submission — **Model A**, the model the submission recommends in
Q3 (one `Order` entity, one `Status` enum, guarded transitions) — and demonstrates,
live, that every illegal state in the problem statement is prevented by the design.

- Requires **Java 17 or above**. No libraries, no build tool.
- Ready to open and run in **VS Code** (config included in `.vscode/`), and also runs
  from a plain terminal / Eclipse.
- Storage is in-memory, for classroom demonstration. The written answer (Q5) explains
  the matching database design — a versioned update / row lock that makes the
  credit check-and-reserve atomic in a real database.

## What it demonstrates

`Main` runs 10 scenarios, **43 checks, all passing**. Each maps to a reported illegal state / question:

| # | Scenario | Design element |
|---|---|---|
| 1 | Empty order places; new order pays/ships/delivers | Guard on every transition; `place()` needs ≥ 1 line (Q1/Q2) |
| 2 | Duplicate product lines; total after adds/removes | Re-adding a product merges quantity; `total()` derived (Q1/Q4) |
| 3 | Happy path DRAFT → PLACED → PAID → SHIPPED → DELIVERED | Guarded transitions; shipment exists iff shipped (Q2) |
| 4 | Cancelled order shipped anyway | `ship()`'s `requireStatus(PAID)` guard refuses it (Q2) |
| 5 | Shipped order cancelled; actions on terminal states | `cancel()` only from Draft/Placed/Paid; Delivered/Cancelled terminal (Q2) |
| 6 | Lines edited after placement | `addLine`/`removeLine` guarded to DRAFT only (Q1) |
| 7 | Shipping without payment; null/blank tracking number | `ship()` requires PAID + a validated `TrackingNumber` (Q2) |
| 8 | Stored total drifting from the lines | No stored total — `total()` recomputed from the lines (Q4) |
| 9 | Order placed over the customer's credit limit | `OrderPlacementService` reserves on `CreditAccount` first (Q5) |
| 10 | Two orders race for the same credit at the same instant | Synchronized atomic `reserve(...)` — exactly one wins (Q5) |

The full sample run is saved in `output.txt`.

## Files

```
src/casestudy2/
  Main.java                 Demo/presentation driver (10 scenarios, PASS/FAIL)
  Order.java                ENTITY / aggregate root - Model A; guarded transitions live here
  Status.java               ENUM - the closed set of states: DRAFT..CANCELLED
  OrderLine.java            VALUE OBJECT - product + unit price + quantity; lineTotal() derived
  Shipment.java             VALUE OBJECT - TrackingNumber + shippedAt (exists iff shipped)
  TrackingNumber.java       VALUE OBJECT - validated, never blank
  Money.java                VALUE OBJECT - exact paise-based amounts (Case Study 1 model)
  Ids.java                  VALUE OBJECTS - OrderId / ProductId / CustomerId wrappers
  CreditAccount.java        AGGREGATE - limit/outstanding/reserved; atomic reserve()
  OrderPlacementService.java  DOMAIN SERVICE - coordinates Order + CreditAccount (Q5)
  DomainException.java      Thrown whenever a business rule is broken
.vscode/
  settings.json             Tells the VS Code Java extension where sources/output are
  launch.json               "Run Case Study 2 Demo" run configuration
```

## How to run — VS Code

1. Unzip the project, then in VS Code: **File → Open Folder** → select `case-study-2-demo`.
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
2. Open `Order.java`, method `ship(...)` — one guard, `requireStatus(Status.PAID)`,
   is the whole reason a cancelled order can never be shipped: a cancelled order is
   in state CANCELLED, so the guard throws before anything is recorded (Scenario 4).
3. Open `Status.java` — six enum values are the *only* states that exist. There is
   no String to typo and no `paid`/`shipped`/`cancelled` flags to contradict
   each other (Scenario 1).
4. Point at `total()` in `Order.java` — there is no total *field* at all. The total
   is summed from the lines on every call, so it cannot drift (Scenarios 2 and 8).
5. Close with `CreditAccount.reserve(...)` — the credit check and the reservation
   are one synchronized step, which is why the two-thread race in Scenario 10
   ends 1–1, never 2–0, and the ₹10,000 limit is never breached (Scenarios 9–10).

## Honest scope

This is the "selected classes and methods" implementation the CO-1 submission asks
for (requirement 5) plus its demonstration (requirement 7) — a domain model with a
console demo, not a full production ordering system: no web UI, no database. Where
the production pieces plug in is covered in the written answer (Q3: persistence of
Model A as one status column; Q5: versioned update / per-account row lock for the
credit reservation) and noted in code comments. The demo implements Model A; Model B
(one type per state) is compared in the written answer (Q3) but is not implemented
here — Model A is the recommended model for a lifecycle with many, often-changing
states.
