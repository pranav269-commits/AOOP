package casestudy5;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory UserRepository for the demo/classroom - no database needed.
 *
 * Methods are synchronized so that check-and-insert is ONE atomic step:
 * two simultaneous registrations with the same email cannot both pass.
 * In the real system this same guarantee comes from a UNIQUE index on
 * the normalised-email column (see Q5 of the written answer).
 */
public final class InMemoryUserRepository implements UserRepository {

    private final Map<String, UserAccount> byEmail = new HashMap<>();

    @Override
    public synchronized void saveNew(UserAccount account) {
        String key = account.email().value();
        if (byEmail.containsKey(key)) {
            throw new DomainException("Email already registered: " + key);
        }
        byEmail.put(key, account);
    }

    @Override
    public synchronized Optional<UserAccount> findByEmail(EmailAddress email) {
        return Optional.ofNullable(byEmail.get(email.value()));
    }

    @Override
    public synchronized boolean existsByEmail(EmailAddress email) {
        return byEmail.containsKey(email.value());
    }

    @Override
    public synchronized void changeEmailKey(EmailAddress oldEmail,
                                            EmailAddress newEmail, UserAccount account) {
        String newKey = newEmail.value();
        UserAccount existing = byEmail.get(newKey);
        if (existing != null && existing != account) {
            throw new DomainException("Email already registered: " + newKey);
        }
        byEmail.remove(oldEmail.value());
        byEmail.put(newKey, account);
    }
}
