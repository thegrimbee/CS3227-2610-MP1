package manhwa.commands;

import java.util.ArrayList;
import java.util.List;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Displays every available detail for every manhwa entry.
 */
public class ListAllCommand extends Command {
    public static final String COMMAND_WORD = "listall";

    private static final String EMPTY_LIST_MESSAGE = "Your list is empty.";
    private static final String LIST_HEADER =
            "Here are the full details of every entry in your list:";

    /**
     * Formats every entry in stored order without changing application data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return every entry's full details or the empty-list message
     * @throws ManhwaTrackerException if stored list access fails
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        if (list.size() == 0) {
            return EMPTY_LIST_MESSAGE;
        }

        List<Manhwa> entries = new ArrayList<>();
        for (int index = 1; index <= list.size(); index++) {
            entries.add(list.get(index));
        }
        return DisplayUtil.formatDetailedEntries(LIST_HEADER, entries, profile, list);
    }
}
