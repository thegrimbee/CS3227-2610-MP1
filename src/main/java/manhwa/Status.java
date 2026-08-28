package manhwa;

/**
 * Represents the reading status of a manhwa.
 */
public enum Status {
    WISHLIST("wishlist"),
    ONGOING("ongoing"),
    COMPLETED("completed");

    private static final String VALID_STATUSES = "wishlist, ongoing, completed";

    private final String displayName;

    Status(String displayName) {
        assert displayName != null;
        this.displayName = displayName;
    }

    /**
     * Returns the lowercase name used to display this status.
     *
     * @return the lowercase display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a status name without regard to letter case.
     *
     * @param value status name to parse
     * @return the matching status
     * @throws ManhwaTrackerException if the supplied name is not a valid status
     */
    public static Status fromString(String value) throws ManhwaTrackerException {
        for (Status status : values()) {
            if (status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new ManhwaTrackerException(
                "Unknown status: " + value + ". Valid statuses: " + VALID_STATUSES + ".");
    }
}
