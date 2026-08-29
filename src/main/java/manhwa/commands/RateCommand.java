package manhwa.commands;

import manhwa.Aspect;
import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Sets or updates one aspect rating for a stored manhwa.
 */
public class RateCommand extends Command {
    public static final String COMMAND_WORD = "rate";

    private final int index;
    private final Aspect aspect;
    private final int rating;

    /**
     * Creates a command for one entry and aspect rating.
     *
     * @param index 1-based entry index
     * @param aspect aspect to rate
     * @param rating rating from 1 to 10
     */
    public RateCommand(int index, Aspect aspect, int rating) {
        assert aspect != null;
        this.index = index;
        this.aspect = aspect;
        this.rating = rating;
    }

    /**
     * Updates and persists the selected rating.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return rating update confirmation
     * @throws ManhwaTrackerException if the index or rating is invalid
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        Manhwa manhwa = list.get(index);
        manhwa.setRating(aspect, rating);
        storage.saveData(list, profile);
        return "Updated: " + manhwa.getTitle() + " - "
                + aspect.getDisplayName() + ": " + rating + "/10";
    }
}
