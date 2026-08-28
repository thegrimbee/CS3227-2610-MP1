package manhwa;

/**
 * Represents a supported ordering for a manhwa list view.
 */
public enum SortKey {
    SCORE("score"),
    TITLE("title"),
    DATE("date"),
    CHAPTERS("chapters"),
    PLOT("plot"),
    ART("art"),
    UNIQUENESS("uniqueness"),
    CHARACTERS("characters"),
    PACING("pacing");

    private static final String VALID_SORT_KEYS =
            "score, title, date, chapters, plot, art, uniqueness, characters, pacing";

    private final String displayName;

    SortKey(String displayName) {
        assert displayName != null;
        this.displayName = displayName;
    }

    /**
     * Returns the lowercase name used to display this sort key.
     *
     * @return the lowercase display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a sort key without regard to letter case.
     *
     * @param value sort key to parse
     * @return the matching sort key
     * @throws ManhwaTrackerException if the supplied value is not a valid sort key
     */
    public static SortKey fromString(String value) throws ManhwaTrackerException {
        for (SortKey sortKey : values()) {
            if (sortKey.displayName.equalsIgnoreCase(value)) {
                return sortKey;
            }
        }
        throw new ManhwaTrackerException(
                "Unknown sort key: " + value + ". Valid: " + VALID_SORT_KEYS + ".");
    }
}
