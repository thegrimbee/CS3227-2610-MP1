package manhwa;

/**
 * Contains the list and optional preference profile loaded from storage.
 */
public final class LoadResult {
    private final ManhwaList manhwaList;
    private final PreferenceProfile preferenceProfile;

    LoadResult(ManhwaList manhwaList, PreferenceProfile preferenceProfile) {
        assert manhwaList != null;
        this.manhwaList = manhwaList;
        this.preferenceProfile = preferenceProfile;
    }

    /**
     * Returns the loaded manhwa list.
     *
     * @return loaded entries
     */
    public ManhwaList getManhwaList() {
        return manhwaList;
    }

    /**
     * Returns the loaded preference profile.
     *
     * @return loaded profile, or {@code null} when none was stored
     */
    public PreferenceProfile getPreferenceProfile() {
        return preferenceProfile;
    }
}
