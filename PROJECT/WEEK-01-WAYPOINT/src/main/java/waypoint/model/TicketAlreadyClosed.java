package waypoint.model;

/**
 * Thrown when close() is called on a ticket that is already closed.
 * Referenced by TicketTest in 7.2 but not supplied in the starter files,
 * so it is defined here. A closed ticket never reopens (invariant I5).
 */
public class TicketAlreadyClosed extends RuntimeException {

    public TicketAlreadyClosed() {
        super("Ticket is already closed");
    }

    public TicketAlreadyClosed(String message) {
        super(message);
    }
}
