package manhwa.commands;

import manhwa.Aspect;
import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Status;
import manhwa.Storage;

/**
 * Changes the reading status of a stored manhwa.
 */
public class StatusCommand extends Command {
    public static final String COMMAND_WORD = "status";

    private final int index;
    private final Status status;

    /**
     * Creates a status-change command for a 1-based entry index.
     *
     * @param index entry index
     * @param status target reading status
     */
    public StatusCommand(int index, Status status) {
        assert status != null;
        this.index = index;
        this.status = status;
    }

    /**
     * Applies an immediate status or starts prompts for missing ratings.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return status confirmation or the first missing-rating prompt
     * @throws ManhwaTrackerException if the entry index is invalid
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        Manhwa manhwa = list.get(index);
        if (status != Status.WISHLIST && hasUnratedAspect(manhwa)) {
            return controller.startStatusFlow(manhwa, status);
        }

        manhwa.setStatus(status);
        storage.saveData(list, profile);
        return getConfirmation(manhwa);
    }

    private boolean hasUnratedAspect(Manhwa manhwa) {
        assert manhwa != null;
        for (Aspect aspect : Aspect.values()) {
            if (manhwa.getRating(aspect) == null) {
                return true;
            }
        }
        return false;
    }

    private String getConfirmation(Manhwa manhwa) {
        assert manhwa != null;
        return "Moved \"" + manhwa.getTitle() + "\" to "
                + status.getDisplayName() + ".";
    }
}
