package casestudy5;

import java.time.Instant;

/**
 * APPLICATION SERVICE - coordinates the aggregates for use cases that
 * no single object can own: registration (email uniqueness spans ALL
 * accounts - Q5), resending verification, changing email, and login.
 *
 * The token secret is returned to the caller here because in a real
 * system this is the point where it would be put into the verification
 * email. The account itself never stores or exposes it for checking -
 * checking happens inside UserAccount.verifyEmail via the token.
 */
public final class RegistrationService {

    private final UserRepository repository;

    public RegistrationService(UserRepository repository) {
        this.repository = repository;
    }

    /** What a successful registration hands back: the account, plus the
     *  token secret that would be emailed to the user. */
    public record RegistrationResult(UserAccount account, String tokenSecret) {
    }

    public RegistrationResult register(String rawEmail, String password) {
        EmailAddress email = EmailAddress.of(rawEmail);            // invalid format dies here
        if (repository.existsByEmail(email)) {
            throw new DomainException("Email already registered: " + email);
        }
        VerificationToken token = VerificationToken.generate(
                email, Instant.now(), VerificationToken.DEFAULT_TTL);
        UserAccount account = UserAccount.register(
                UserId.generate(), email, token, Passwords.hash(password));
        repository.saveNew(account);                               // atomic re-check (race-safe)
        return new RegistrationResult(account, token.secret());
    }

    /** Resend: a NEW token replaces the old one, which stops working. */
    public String resendVerification(UserAccount account) {
        VerificationToken token = VerificationToken.generate(
                account.email(), Instant.now(), VerificationToken.DEFAULT_TTL);
        account.reissueToken(token);
        return token.secret();
    }

    /** Change email: uniqueness is re-checked for the NEW address, and
     *  the account goes back to PendingVerification (see UserAccount). */
    public String changeEmail(UserAccount account, String rawNewEmail) {
        EmailAddress newEmail = EmailAddress.of(rawNewEmail);
        VerificationToken token = VerificationToken.generate(
                newEmail, Instant.now(), VerificationToken.DEFAULT_TTL);
        repository.changeEmailKey(account.email(), newEmail, account);  // uniqueness first
        account.changeEmail(newEmail, token);
        return token.secret();
    }

    /**
     * Login: password first, then the single status gate.
     * Suspended, pending and deleted accounts are all refused here -
     * there is no second flag anywhere that could disagree.
     */
    public UserAccount login(String rawEmail, String password) {
        EmailAddress email = EmailAddress.of(rawEmail);
        UserAccount account = repository.findByEmail(email)
                .orElseThrow(() -> new DomainException("Login failed: account not available"));
        if (!account.passwordMatches(password)) {
            throw new DomainException("Login failed: incorrect password");
        }
        if (!account.canLogin()) {
            throw new DomainException(
                    "Login failed: account is " + account.status() + " and cannot log in");
        }
        return account;
    }
}
