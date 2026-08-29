package manhwa.commands;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Removes a tag from a stored manhwa entry.
 */
public class UntagCommand extends Command {
    public static final String COMMAND_WORD = "untag";

    private final int index;
    private final String tag;

    /**
     * Creates a command for one entry and tag.
     *
     * @param index 1-based entry index
     * @param tag tag to remove
     */
    public UntagCommand(int index, String tag) {
        assert tag != null;
        this.index = index;
        this.tag = tag;
    }

    /**
     * Removes and persists an existing tag.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return removal confirmation or a message that the tag is absent
     * @throws ManhwaTrackerException if the entry index is invalid
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        Manhwa manhwa = list.get(index);
        if (!manhwa.getTags().contains(tag)) {
            return "\"" + manhwa.getTitle() + "\" has no tag #" + tag + ".";
        }
        manhwa.removeTag(tag);
        storage.saveData(list, profile);
        return "Removed tag #" + tag + " from \"" + manhwa.getTitle() + "\".";
    }
}
