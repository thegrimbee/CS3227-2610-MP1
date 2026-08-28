package manhwa;

/**
 * Represents an error that can be shown to a ManhwaDex Lite user.
 */
public class ManhwaTrackerException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the specified user-facing message.
     *
     * @param message message describing the error
     */
    public ManhwaTrackerException(String message) {
        super(message);
        assert message != null;
    }
}
