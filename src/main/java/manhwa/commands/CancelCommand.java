package manhwa.commands;

import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Reports that there is no interactive flow to cancel while the controller is idle.
 */
public class CancelCommand extends Command {
    public static final String COMMAND_WORD = "cancel";

    private static final String NOTHING_TO_CANCEL_MESSAGE = "Nothing to cancel.";

    /**
     * Returns the idle-state cancellation response without changing stored data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return message explaining that no flow is active
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) {
        assert list != null;
        assert storage != null;
        assert controller != null;
        return NOTHING_TO_CANCEL_MESSAGE;
    }
}
