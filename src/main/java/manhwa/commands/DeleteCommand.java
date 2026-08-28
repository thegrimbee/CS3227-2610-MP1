package manhwa.commands;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Deletes one entry using its 1-based list index.
 */
public class DeleteCommand extends Command {
    public static final String COMMAND_WORD = "delete";

    private static final String DELETE_MESSAGE_PREFIX =
            "Noted. I've removed this entry:";
    private static final String ENTRY_INDENT = "  ";
    private static final String COUNT_MESSAGE_PREFIX = "You now have ";
    private static final String COUNT_MESSAGE_SUFFIX = " entries.";

    private final int index;

    /**
     * Creates a command that deletes an entry at a 1-based index.
     *
     * @param index entry index
     */
    public DeleteCommand(int index) {
        this.index = index;
    }

    /**
     * Deletes and persists the selected entry.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return confirmation containing the deleted entry and updated count
     * @throws ManhwaTrackerException if the index is outside the list
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        Manhwa deletedEntry = list.delete(index);
        storage.saveData(list, profile);
        return DELETE_MESSAGE_PREFIX
                + System.lineSeparator()
                + ENTRY_INDENT + DisplayUtil.formatEntry(deletedEntry, profile)
                + System.lineSeparator()
                + COUNT_MESSAGE_PREFIX + list.size() + COUNT_MESSAGE_SUFFIX;
    }
}
