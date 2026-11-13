# CO-1 Case Study 5 — Demo Application

**AOOP (25CS2205E) · Case Study 5: User Accounts, Email Verification and Contact Details**

A small, dependency-free Java application that implements the domain model from our
Case Study 5 written submission, and demonstrates — live — that every bug reported in
the problem statement is prevented by the design.

- Requires **Java 17 or above** (uses records and sealed types). No libraries, no build tool.
- Storage is in-memory (for classroom demonstration). The written answer explains the
  matching database design — a UNIQUE index on the normalised email column — in Q5.

## What it demonstrates

`Main` runs 10 scenarios, **38 checks, all passing**. Each scenario is one reported bug:

| # | Reported bug | Design element that prevents it |
|---|---|---|
| 1 | Registrations with incorrectly formatted emails | `EmailAddress.of(...)` validates at construction |
| 2 | (baseline) Account must start unverified | `UserAccount.register(...)` always starts `PendingVerification` |
| 3 | Duplicate accounts, same email, different capitalisation | Lower-case normalisation + uniqueness in `RegistrationService` / repository |
| 4 | Verification tokens accepted after expiry | Expiry inside `VerificationToken`; checked in `isValidFor(...)` |
| 5 | Users "verified" with no verification date | `Verified(Instant)` — the state carries its date; cannot exist without it |
| 6 | (hardening) Old verification link stays usable | Resend replaces the pending token; the old secret stops working |
| 7 | Phone numbers stored in different formats | `PhoneNumber.parse(...)` canonicalises to E.164 (`+919876543210`) |
| 8 | Postal addresses with incomplete information | `PostalAddress` requires all parts; `Optional<PostalAddress>` = complete or absent |
| 9 | Verified trust carried to a changed email | `changeEmail(...)` resets verification to pending for the new address |
| 10 | Suspended users still able to log in | Single gate: `canLogin()` is true only for `ACTIVE`; login goes through it |

The full sample run is saved in `output.txt`.

## Files

```
src/casestudy5/
  Main.java                   Demo/presentation driver (10 scenarios, PASS/FAIL)
  UserAccount.java            ENTITY / aggregate root - all rules enforced here
  EmailVerification.java      Sealed state type: Unverified / PendingVerification / Verified
  AccountStatus.java          Enum: PENDING_VERIFICATION / ACTIVE / SUSPENDED / DELETED
  EmailAddress.java           VALUE OBJECT - validation + normalisation
  PhoneNumber.java            VALUE OBJECT - canonical E.164 form
  PostalAddress.java          VALUE OBJECT - complete-or-rejected address
  VerificationToken.java      VALUE OBJECT - secret + issued-for + expiry, self-validating
  UserId.java                 VALUE OBJECT - typed identity
  RegistrationService.java    Application service: register / resend / change email / login
  UserRepository.java         Repository interface (uniqueness contract)
  InMemoryUserRepository.java In-memory store; atomic check-and-insert (DB unique-index stand-in)
  Passwords.java              Demo password hashing (SHA-256 - production: BCrypt/Argon2)
  DomainException.java        Thrown whenever a business rule is broken
```

## How to run

### Option A — Command Prompt (CMD) on Windows

Open the project folder in File Explorer, click the address bar, type `cmd`, press Enter
(a CMD window opens already inside the folder). Then run, in **CMD**:

```bat
javac -d out src\casestudy5\*.java
java -cp out casestudy5.Main
```

You should see `RESULT: 38 passed, 0 failed`.

### Option B — Eclipse (as in the AOOP practical sessions)

1. File → New → Java Project, name it `case-study-5-demo`, use Java 17+.
2. Right-click the project → Import → General → File System → select this project's
   `src` folder so the `casestudy5` package lands under the project's `src`.
   (Or simply create package `casestudy5` and copy the `.java` files into it.)
3. Open `Main.java` → Run As → Java Application.

## Presentation walkthrough (2 minutes)

1. Run `Main` once — point at the final line: **38 passed, 0 failed**.
2. Open `EmailVerification.java` — one sealed type replaces four loose fields
   (`emailVerified`, `verifiedAt`, `verificationToken`, `tokenExpiry`).
   "Verified with no date" cannot even be constructed.
3. Open `EmailAddress.java` — validation and lower-casing happen once, in the factory;
   that single decision kills both the malformed-email bug and the duplicate-account bug.
4. Open `UserAccount.verifyEmail(...)` — the token checks its own expiry;
   no caller can forget the check because there is no other way to become verified.
5. Close with Q5: email uniqueness is *not* a `UserAccount` invariant — it belongs to the
   registration service plus a database unique index, which is why `saveNew` in the
   repository re-checks atomically.

## Honest scope

This is the "selected classes and methods" implementation the CO-1 submission asks for
(requirement 5) plus its demonstration (requirement 7) — a domain model with a console
demo, not a full production SaaS app: no web UI, no real email sending, no database.
Where each production piece would plug in is noted in the code comments
(token hashing at rest, BCrypt for passwords, DB unique index for email).
