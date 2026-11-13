package casestudy5;

/**
 * VALUE (closed enumeration) - the account lifecycle status.
 * A closed set, unlike a free String: "SUSPNDED" (typo) cannot be stored,
 * and every rule that depends on status switches over all four values.
 */
public enum AccountStatus {
    PENDING_VERIFICATION,   // registered, email not yet verified - cannot log in
    ACTIVE,                 // verified - the ONLY status that can log in
    SUSPENDED,              // blocked by admin/user - cannot log in
    DELETED                 // terminal - can never log in or be reactivated
}
