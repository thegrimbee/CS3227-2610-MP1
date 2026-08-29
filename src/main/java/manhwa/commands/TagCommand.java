package manhwa.commands;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Adds a tag to a stored manhwa entry.
 */
public class TagCommand extends Command {
    public static final String COMMAND_WORD = "tag";

    private final int index;
    private final String tag;

    /**
     * Creates a command for one entry and tag.
     *
     * @param index 1-based entry index
     * @param tag tag to add
     */
    public TagCommand(int index, String tag) {
        assert tag != null;
        this.index = index;
        this.tag = tag;
    }

    /**
     * Adds and persists the tag using the domain model's validation.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return tag addition confirmation
     * @throws ManhwaTrackerException if the index or tag is invalid
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        Manhwa manhwa = list.get(index);
        manhwa.addTag(tag);
        storage.saveData(list, profile);
        return "Added tag #" + tag + " to \"" + manhwa.getTitle() + "\".";
    }
}
