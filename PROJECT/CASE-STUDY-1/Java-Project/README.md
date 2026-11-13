# CO-1 Case Study 1 — Demo Application (Word-doc matching version)

**AOOP (25CS2205E) · Case Study 1: Money and Pricing in an E-Commerce Catalog**

This version is rebuilt so the code matches the **original Word doc
(`CO-1-Case-Study-1.docx`) Refined Model skeleton as closely as possible** —
same class names, same field names, same method signatures — while remaining a
complete, runnable program. The earlier `case-study-1-demo` project demonstrated
the same design with slightly different names (`minorUnits` on `Money`, `scale`
on `Currency`, a nested `Cart.Line`); this project uses the doc's own terms.

- Requires **Java 17 or above**. No libraries, no build tool.
- Ready to open and run in **VS Code** (config included in `.vscode/`), and also runs
  from a plain terminal / Eclipse.
- Storage is in-memory, for classroom demonstration. There is no database or UI —
  see "Honest scope" below.

## Word doc skeleton → this project

| Word doc skeleton (Refined Model / Classification) | File in this project | Notes |
|---|---|---|
| `enum Currency { EUR(2), USD(2), JPY(0), KWD(3); final int minorUnits; ... }` | `src/casestudy1/Currency.java` | Field is `minorUnits` exactly as in the doc; a `factor()` helper (10^minorUnits) is added for formatting/conversion. |
| `final class Money { private final long minor; private final Currency currency; ... }` | `src/casestudy1/Money.java` | Field is `minor` (accessor `minor()`), not `minorUnits`. |
| `static Money of(String decimal, Currency c)` — rejects negatives and excess precision | `Money.java` — `Money.of(String, Currency)` | Primary factory. `Money.of("10.505", EUR)` is rejected, not rounded. A helper `ofMinor(long, Currency)` (and `zero(Currency)`) is also provided, clearly marked as a helper, for minor-unit construction. |
| `Money plus(Money o) { requireSame(o); ... }` | `Money.java` — `plus(Money)` | Exact minor-unit addition, same currency only. |
| `Money times(Quantity q)` | `Money.java` — `times(Quantity)` | Exact integer multiplication. |
| `Money minus(Percentage p)` — rounds HALF_EVEN, result >= 0 | `Money.java` — `minus(Percentage)` | Delegates to `Percentage.applyTo(Money)`, so rounding (HALF_EVEN) lives in ONE place and the two always agree. `minus(Money)` is also provided. |
| `private void requireSame(Money o) { if (currency != o.currency) throw new CurrencyMismatch(); }` | `Money.java` — `requireSame(Money)` + `src/casestudy1/CurrencyMismatch.java` | Mixed-currency `plus`/`minus` throw the dedicated `CurrencyMismatch` exception exactly as the doc shows. |
| `final class Percentage { private final int basisPoints; /* 0..10000 */ static Percentage ofBasisPoints(int bp) { if (bp < 0 \|\| bp > 10_000) throw ...; } }` | `src/casestudy1/Percentage.java` | Private constructor (Q3), `ofBasisPoints(int)` as in the doc, plus `ofPercent(int)` convenience factory. |
| Q3: `Percentage` supports `applyTo(Money)`, `equals`/`hashCode` | `Percentage.java` — `applyTo(Money)`, `equals`, `hashCode` | `applyTo` returns the **discounted** price: `discounted = minor * (10000 - bp) / 10000`, rounded HALF_EVEN. 15% off 100.00 EUR = 85.00 EUR. |
| `final class Quantity { private final int value; /* >= 1 */ static Quantity of(int v) { if (v < 1) throw ...; } }` | `src/casestudy1/Quantity.java` | Plus `plus(Quantity)` — the doc's Classification table gives Quantity add/subtract behaviour. |
| Classification: `Product` = Entity, identified by SKU | `src/casestudy1/Product.java` | Price is a `Money`. Equals/hashCode by SKU only. |
| Classification: `Cart` = Entity / aggregate root | `src/casestudy1/Cart.java` | Lines merged by SKU, encapsulated, `total()` derived (never stored). |
| Classification: `CartLine` = entity inside the Cart aggregate / line merged by SKU | `src/casestudy1/CartLine.java` | Separate file (not a nested type). Holds `Product` + `Quantity`; `lineTotal()` derived. |
| Discussion/Q4: conversion needs a rate and a date, in a domain service | `src/casestudy1/ExchangeService.java` | Extension beyond the skeleton: `convert(amount, to, rate, date)` — explicit, pure, repeatable. |
| Discussion/Q5: refunds via a separate signed/adjustment type, price stays non-negative | `src/casestudy1/Refund.java`, `src/casestudy1/SignedMoney.java` | Extensions beyond the skeleton: `Refund` magnitude is a positive `Money` + payment id; ledger-only `SignedMoney` may be negative, `requireNonNegative()` is the only way back to `Money`. |
| — (supporting) | `src/casestudy1/DomainException.java`, `src/casestudy1/Main.java` | `DomainException` (extends `IllegalArgumentException`) for invariant violations other than currency mismatch; `Main` is the demo driver. |

> **The naive model is intentionally NOT implemented.** The doc's Naive Model
> (`Product { String name; double price; String currency; double discountPercent; }`,
> `CartItem { Product product; int quantity; }`) is the broken starting point the
> case study asks you to fix — `double` rounding, free-string currency, unchecked
> discount/quantity. Implementing it would reintroduce every reported bug, so
> `Product` here uses a `Money` price and `CartLine` uses a `Quantity`.

## What it demonstrates

`Main` runs 10 scenarios, **46 checks, all passing**. Each maps to a reported bug / question:

| # | Scenario | Design element |
|---|---|---|
| 1 | `double` fails (0.1 + 0.2 != 0.3); Money in minor units is exact | `Money` as `long minor` + `Currency` (Q1) |
| 2 | Minor-unit precision per currency: EUR/USD 2, JPY 0, KWD 3; formatting respects it | `Currency.minorUnits` + `Money.of(String, Currency)` (Q1) |
| 3 | Negative Money / negative price rejected; excess precision (`10.505` EUR) rejected, not rounded | `Money` private constructor + `Money.of` validation (Q1/Q2) |
| 4 | Mixed-currency plus/minus rejected with `CurrencyMismatch` (EUR + USD) | `Money.requireSame` → `CurrencyMismatch`; no silent conversion (Q4) |
| 5 | Percentage range: -1%, 101%, 250%, negative / >10,000 basis points rejected | `Percentage` private constructor + factories (Q3) |
| 6 | `applyTo`: 15% off 100.00 EUR = 85.00 EUR; `Money.minus(Percentage)` agrees; 0% / 100%; HALF_EVEN cases (15% off 10.50 EUR = 8.92 EUR) | `Percentage.applyTo(Money)` — HALF_EVEN in one place (Q3) |
| 7 | Quantity 0 / negative rejected; Money × Quantity is exact | `Quantity` positive-int value object (Q2) |
| 8 | Cart merges duplicate products into one `CartLine`; `total()` derived and agrees two independent ways | `Cart` aggregate root, encapsulated lines, derived total (Q1/Q2) |
| 9 | Explicit conversion with stated rate and date works and is repeatable | `ExchangeService.convert(amount, to, rate, date)` (Q4) |
| 10 | Refunds: positive magnitude + payment id; ledger nets to 70.00 EUR; negative `SignedMoney` cannot become a price | `Refund` + ledger-only `SignedMoney` with `requireNonNegative()` (Q5) |

The full sample run is saved in `output.txt`.

## Files

```
src/casestudy1/
  Main.java               Demo/presentation driver (10 scenarios, PASS/FAIL)
  Money.java              VALUE OBJECT - doc skeleton: long minor, of(String, Currency), plus/times/minus, requireSame
  Currency.java           VALUE OBJECT (enum) - doc skeleton: EUR/USD (2), JPY (0), KWD (3) minorUnits
  CurrencyMismatch.java   Dedicated exception thrown by Money.requireSame, exactly as in the doc
  Percentage.java         VALUE OBJECT - doc skeleton: basisPoints, private constructor, ofBasisPoints, applyTo() with HALF_EVEN (Q3)
  Quantity.java           VALUE OBJECT - doc skeleton: value >= 1, Quantity.of()
  Product.java            ENTITY - identified by SKU; price is a Money
  Cart.java               ENTITY / aggregate root - lines unique by product, encapsulated, total() derived
  CartLine.java           Line inside the Cart aggregate - Product + Quantity, lineTotal() derived
  ExchangeService.java    DOMAIN SERVICE - explicit conversion; rate and date are inputs (Q4)
  Refund.java             VALUE OBJECT - positive magnitude + payment id + reason; direction by type (Q5)
  SignedMoney.java        VALUE OBJECT, ledger-only - may be negative; requireNonNegative() is the only way back
  DomainException.java    Thrown whenever a business rule other than currency mismatch is broken (extends IllegalArgumentException)
.vscode/
  settings.json           Tells the VS Code Java extension where sources/output are
  launch.json             "Run Case Study 1 Demo" run configuration
```

## How to run — VS Code

1. Unzip the project, then in VS Code: **File → Open Folder** → select `case-study-1-docmatch`.
2. Make sure the **Extension Pack for Java** (Microsoft) is installed — VS Code usually
   prompts for it the moment it sees `.java` files. Let the Java language server finish
   importing (watch the bottom-right corner).
3. Open `src/casestudy1/Main.java` and either:
   - click **Run** above the `main` method, or
   - press **F5** and pick **Run Case Study 1 Demo**, or
   - use the integrated terminal (see below).

You should see `RESULT: 46 passed, 0 failed`.

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

1. Run `Main` — point at the final line: **46 passed, 0 failed**.
2. Open `Money.java` — side by side with the Word doc: same fields (`minor`, `currency`),
   same `of(String, Currency)` factory, same `requireSame` throwing `CurrencyMismatch`.
   Amounts are `long` minor units, never a `double`, so Scenario 1's cent disagreement
   cannot happen; the private constructor is also why a negative price is unbuildable (Scenario 3).
3. Open `Percentage.java` — exactly the Q3 code: `basisPoints`, private constructor, and
   the single HALF_EVEN rounding rule inside `applyTo` (Scenarios 5–6: 15% off
   100.00 EUR is 85.00 EUR, exactly; `Money.minus(Percentage)` delegates here).
4. Open `Money.plus` vs `ExchangeService.java` — plus *refuses* mixed currencies with
   `CurrencyMismatch`; conversion only happens when a caller states a rate and a
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
