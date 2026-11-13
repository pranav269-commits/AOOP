package casestudy5;

import java.util.Optional;

/**
 * Storage for accounts. Email uniqueness lives HERE and in the
 * registration service, not inside UserAccount - one account cannot
 * see the others (Q5: a cross-aggregate rule).
 */
public interface UserRepository {

    /**
     * Saves a NEW account. Must fail atomically if an account with the
     * same normalised email already exists - this is the in-memory
     * stand-in for a database UNIQUE constraint on the email column.
     */
    void saveNew(UserAccount account);

    Optional<UserAccount> findByEmail(EmailAddress email);

    boolean existsByEmail(EmailAddress email);

    /** Re-keys an account after an email change, re-checking uniqueness. */
    void changeEmailKey(EmailAddress oldEmail, EmailAddress newEmail, UserAccount account);
}
