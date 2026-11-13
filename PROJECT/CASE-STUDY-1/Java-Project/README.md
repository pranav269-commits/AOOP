# CO-1 Case Study 1 — Demo Application

**AOOP (25CS2205E) · Case Study 1: Money and Pricing in an E-Commerce Catalog**

A small, dependency-free Java application that implements the domain model from the
Case Study 1 written submission, and demonstrates — live — that every bug reported in
the problem statement is prevented by the design.

- Requires **Java 17 or above** (records). No libraries, no build tool.
- Ready to open and run in **VS Code** (config included in `.vscode/`), and also runs
  from a plain terminal / Eclipse.
- Storage is in-memory, for classroom demonstration. There is no database or UI —
  see "Honest scope" below.

## What it demonstrates

`Main` runs 10 scenarios, **45 checks, all passing**. Each maps to a reported bug / question:

| # | Scenario | Design element |
|---|---|---|
| 1 | `double` fails (0.1 + 0.2 != 0.3); Money in minor units is exact | `Money` as `long` minor units + `Currency` (Q1) |
| 2 | Minor-unit scales per currency: EUR/USD 2, JPY 0, KWD 3; formatting respects scale | `Currency` scale + `Money.toString()` (Q1) |
| 3 | Negative Money / negative price rejected | `Money` private constructor rejects negatives; `Product.price` is a `Money` (Q1/Q2) |
| 4 | Mixed-currency plus rejected (EUR + USD) | `Money.plus`/`minus` currency check; no silent conversion (Q4) |
| 5 | Percentage range: -1%, 101%, 250%, negative / >10,000 basis points rejected | `Percentage` private constructor + factories (Q3) |
| 6 | `applyTo`: 15% off 100.00 EUR = 85.00 EUR; 0% / 100%; HALF_EVEN rounding cases | `Percentage.applyTo(Money)` — HALF_EVEN in one place (Q3) |
| 7 | Quantity 0 / negative rejected; Money × Quantity is exact | `Quantity` positive-int value object (Q2) |
| 8 | Cart merges duplicate products; `total()` derived and agrees two independent ways | `Cart` aggregate root, encapsulated lines, derived total (Q1/Q2) |
| 9 | Explicit conversion with stated rate and date works and is repeatable | `ExchangeService.convert(amount, to, rate, date)` (Q4) |
| 10 | Refunds: positive magnitude + payment id; ledger nets to 70.00 EUR; negative `SignedMoney` cannot become a price | `Refund` + ledger-only `SignedMoney` with `requireNonNegative()` (Q5) |

The full sample run is saved in `output.txt`.

## Files

```
src/casestudy1/
  Main.java               Demo/presentation driver (10 scenarios, PASS/FAIL)
  Money.java              VALUE OBJECT - exact minor-unit amount, never negative, currency-checked plus/minus
  Currency.java           VALUE OBJECT (enum) - EUR/USD (2), JPY (0), KWD (3) minor units
  Percentage.java         VALUE OBJECT - basis points, private constructor, applyTo() with HALF_EVEN (Q3)
  Quantity.java           VALUE OBJECT - positive int only
  Product.java            ENTITY - identified by SKU; price is a Money
  Cart.java               ENTITY / aggregate root - lines unique by product, encapsulated, total() derived
  ExchangeService.java    DOMAIN SERVICE - explicit conversion; rate and date are inputs (Q4)
  Refund.java             VALUE OBJECT - positive magnitude + payment id + reason; direction by type (Q5)
  SignedMoney.java        VALUE OBJECT, ledger-only - may be negative; requireNonNegative() is the only way back
  DomainException.java    Thrown whenever a business rule is broken (extends IllegalArgumentException)
.vscode/
  settings.json           Tells the VS Code Java extension where sources/output are
  launch.json             "Run Case Study 1 Demo" run configuration
```

## How to run — VS Code

1. Unzip the project, then in VS Code: **File → Open Folder** → select `case-study-1-demo`.
2. Make sure the **Extension Pack for Java** (Microsoft) is installed — VS Code usually
   prompts for it the moment it sees `.java` files. Let the Java language server finish
   importing (watch the bottom-right corner).
3. Open `src/casestudy1/Main.java` and either:
   - click **Run** above the `main` method, or
   - press **F5** and pick **Run Case Study 1 Demo**, or
   - use the integrated terminal (see below).

You should see `RESULT: 45 passed, 0 failed`.

### Or from the integrated terminal in VS Code (PowerShell)

Open the folder in VS Code, open the terminal with Ctrl + ` , then run, in **PowerShell**:

```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out casestudy1.Main
```

### Or from Command Prompt (CMD) on Windows

Open the project folder, click the address bar, type `cmd`, press Enter, then run, in **CMD**:

```bat
javac -d out src\casestudy1\*.java
java -cp out casestudy1.Main
```

## Presentation walkthrough (2 minutes)

1. Run `Main` — point at the final line: **45 passed, 0 failed**.
2. Open `Money.java` — amounts are `long` minor units, never a `double`, so
   Scenario 1's cent disagreement cannot happen; the private constructor is
   also why a negative price is unbuildable (Scenario 3).
3. Open `Percentage.java` — exactly the Q3 code: basis points, private
   constructor, and the single HALF_EVEN rounding rule inside `applyTo`
   (Scenarios 5–6: 15% off 100.00 EUR is 85.00 EUR, exactly).
4. Open `Money.plus` vs `ExchangeService.java` — plus *refuses* mixed
   currencies; conversion only happens when a caller states a rate and a
   date out loud (Scenarios 4 and 9).
5. Close with `Refund.java` / `SignedMoney.java` — a refund's direction is
   its type, its magnitude stays positive, and only the ledger gets a
   signed type, fenced off from prices by the compiler (Scenario 10).

## Honest scope

This is the "selected classes and methods" implementation the CO-1 submission asks
for plus its demonstration — a domain model with a console demo, not a full
production e-commerce system: no web UI, no database, no real payment or
foreign-exchange feed. In production the exchange rate in Scenario 9 would come
from a rate provider *for the stated date* and be recorded with the conversion
for audit; the demo passes it in explicitly, which is exactly the design point.
