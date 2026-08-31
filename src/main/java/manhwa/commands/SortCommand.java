package manhwa.commands;

import java.util.List;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.PreferenceProfile;
import manhwa.SortKey;
import manhwa.Storage;

/**
 * Displays a sorted view of the stored manhwa entries.
 */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";

    private static final String EMPTY_LIST_MESSAGE = "Your list is empty.";
    private static final String LIST_HEADER = "Here are the entries in your list:";

    private final SortKey sortKey;

    /**
     * Creates a command for the selected sort key.
     *
     * @param sortKey ordering to apply
     */
    public SortCommand(SortKey sortKey) {
        assert sortKey != null;
        this.sortKey = sortKey;
    }

    /**
     * Produces a sorted display without changing or saving the stored order.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return sorted entry display or the empty-list message
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) {
        assert list != null;
        assert storage != null;
        assert controller != null;
        if (list.size() == 0) {
            return EMPTY_LIST_MESSAGE;
        }
        PreferenceProfile sortingProfile = profile == null
                ? new PreferenceProfile() : profile;
        List<Manhwa> sortedEntries = list.sortedView(sortKey, sortingProfile);
        return DisplayUtil.formatEntries(LIST_HEADER, sortedEntries, profile, list);
    }
}
