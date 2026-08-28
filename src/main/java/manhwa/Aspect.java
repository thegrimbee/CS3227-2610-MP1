package manhwa;

/**
 * Represents an aspect on which a manhwa can be rated.
 */
public enum Aspect {
    PLOT("plot"),
    ART("art"),
    UNIQUENESS("uniqueness"),
    CHARACTERS("characters"),
    PACING("pacing");

    private static final String VALID_ASPECTS =
            "plot, art, uniqueness, characters, pacing";

    private final String displayName;

    Aspect(String displayName) {
        assert displayName != null;
        this.displayName = displayName;
    }

    /**
     * Returns the lowercase name used to display this aspect.
     *
     * @return the lowercase display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses an aspect name without regard to letter case.
     *
     * @param value aspect name to parse
     * @return the matching aspect
     * @throws ManhwaTrackerException if the supplied name is not a valid aspect
     */
    public static Aspect fromString(String value) throws ManhwaTrackerException {
        for (Aspect aspect : values()) {
            if (aspect.displayName.equalsIgnoreCase(value)) {
                return aspect;
            }
        }
        throw new ManhwaTrackerException(
                "Unknown aspect: " + value + ". Valid aspects: " + VALID_ASPECTS + ".");
    }
}
