package casestudy5;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * ENTITY and AGGREGATE ROOT - one user account.
 *
 * Identity (UserId) survives every change: email, phone, address and
 * status all change during the account's life, yet it is the same
 * account. All state changes go through its methods - every field is
 * private and there are NO setters - so the invariants cannot be
 * bypassed from outside:
 *
 *   - an account starts PendingVerification, never "pre-verified"
 *   - verifyEmail is the only road to Verified, and it goes through
 *     the token's own isValidFor check (secret + email + expiry)
 *   - changing the email resets verification for the new address
 *   - only ACTIVE accounts can log in (see canLogin)
 */
public final class UserAccount {

    private final UserId id;
    private final String passwordHash;               // hash only - see Passwords
    private EmailAddress email;
    private EmailVerification verification;
    private Optional<PhoneNumber> phone = Optional.empty();
    private Optional<PostalAddress> address = Optional.empty();
    private AccountStatus status;

    private UserAccount(UserId id, EmailAddress email,
                        VerificationToken firstToken, String passwordHash) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.verification = new EmailVerification.PendingVerification(
                Objects.requireNonNull(firstToken));
        this.status = AccountStatus.PENDING_VERIFICATION;
    }

    /** Registration: a new account is ALWAYS unverified and pending. */
    public static UserAccount register(UserId id, EmailAddress email,
                                       VerificationToken firstToken, String passwordHash) {
        return new UserAccount(id, email, firstToken, passwordHash);
    }

    // ------------------------------------------------------------------
    // Email verification
    // ------------------------------------------------------------------

    public void verifyEmail(String presentedSecret, Instant now) {
        if (!(verification instanceof EmailVerification.PendingVerification pending)) {
            throw new DomainException("Email is not awaiting verification (state: "
                    + verification.getClass().getSimpleName() + ")");
        }
        VerificationToken token = pending.token();
        if (token.isExpired(now)) {
            throw new DomainException("Verification token has expired - request a new one");
        }
        if (!token.isValidFor(email, presentedSecret, now)) {
            throw new DomainException("Verification token is incorrect");
        }
        this.verification = new EmailVerification.Verified(now);   // token consumed & gone
        if (status == AccountStatus.PENDING_VERIFICATION) {
            status = AccountStatus.ACTIVE;
        }
    }

    /** Issues a replacement token (resend). The old token dies here,
     *  because the PendingVerification holding it is replaced. */
    public void reissueToken(VerificationToken newToken) {
        if (verification instanceof EmailVerification.Verified) {
            throw new DomainException("Email is already verified");
        }
        this.verification = new EmailVerification.PendingVerification(
                Objects.requireNonNull(newToken));
    }

    /** Changing the email RESETS verification: trust was earned for the
     *  old inbox, not for this one (Invariant 8 in the written answer). */
    public void changeEmail(EmailAddress newEmail, VerificationToken tokenForNewEmail) {
        requireNotDeleted("change email");
        this.email = Objects.requireNonNull(newEmail);
        this.verification = new EmailVerification.PendingVerification(
                Objects.requireNonNull(tokenForNewEmail));
        if (status == AccountStatus.ACTIVE) {
            status = AccountStatus.PENDING_VERIFICATION;   // not fully active until re-verified
        }
    }

    public boolean isVerified() {
        return verification instanceof EmailVerification.Verified;
    }

    // ------------------------------------------------------------------
    // Contact details - optional, but complete-or-absent
    // ------------------------------------------------------------------

    public void setPhone(PhoneNumber phone) {
        requireNotDeleted("change phone");
        this.phone = Optional.of(Objects.requireNonNull(phone));
    }

    public void setAddress(PostalAddress address) {
        requireNotDeleted("change address");
        this.address = Optional.of(Objects.requireNonNull(address));
    }

    // ------------------------------------------------------------------
    // Lifecycle / login gate
    // ------------------------------------------------------------------

    /** THE login rule, in exactly one place: only ACTIVE may log in. */
    public boolean canLogin() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean passwordMatches(String candidatePassword) {
        return Passwords.hash(candidatePassword).equals(passwordHash);
    }

    public void suspend() {
        requireNotDeleted("suspend");
        status = AccountStatus.SUSPENDED;
    }

    public void reactivate() {
        if (status != AccountStatus.SUSPENDED) {
            throw new DomainException("Only a suspended account can be reactivated (state: "
                    + status + ")");
        }
        if (!isVerified()) {
            throw new DomainException("A suspended account must be verified before reactivation");
        }
        status = AccountStatus.ACTIVE;
    }

    public void delete() {
        status = AccountStatus.DELETED;   // terminal: no method leaves this state
    }

    private void requireNotDeleted(String action) {
        if (status == AccountStatus.DELETED) {
            throw new DomainException("Cannot " + action + " on a deleted account");
        }
    }

    // ------------------------------------------------------------------
    // Read-only access
    // ------------------------------------------------------------------

    public UserId id() { return id; }
    public EmailAddress email() { return email; }
    public EmailVerification verification() { return verification; }
    public Optional<PhoneNumber> phone() { return phone; }
    public Optional<PostalAddress> address() { return address; }
    public AccountStatus status() { return status; }

    @Override
    public String toString() {
        return "UserAccount[" + email + ", status=" + status
                + ", verification=" + verification.getClass().getSimpleName() + "]";
    }
}
