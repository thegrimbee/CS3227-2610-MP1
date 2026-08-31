package manhwa.commands;

import java.util.ArrayList;
import java.util.List;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Status;
import manhwa.Storage;

/**
 * Displays all entries or those with a selected reading status.
 */
public class ListCommand extends Command {
    public static final String COMMAND_WORD = "list";

    private static final String EMPTY_LIST_MESSAGE = "Your list is empty.";
    private static final String LIST_HEADER = "Here are the entries in your list:";

    private final Status status;

    /**
     * Creates a command that displays all entries.
     */
    public ListCommand() {
        this(null);
    }

    /**
     * Creates a command that displays entries with the specified status.
     *
     * @param status status to display
     */
    public ListCommand(Status status) {
        this.status = status;
    }

    /**
     * Formats the selected entries without changing stored data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return the formatted list or empty-list message
     * @throws ManhwaTrackerException if stored list access fails
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        List<Manhwa> entries = getSelectedEntries(list);
        if (entries.isEmpty()) {
            return EMPTY_LIST_MESSAGE;
        }
        return DisplayUtil.formatEntries(LIST_HEADER, entries, profile, list);
    }

    private List<Manhwa> getSelectedEntries(ManhwaList list) throws ManhwaTrackerException {
        if (status != null) {
            return list.filterByStatus(status);
        }
        List<Manhwa> entries = new ArrayList<>();
        for (int index = 1; index <= list.size(); index++) {
            entries.add(list.get(index));
        }
        return entries;
    }
}
