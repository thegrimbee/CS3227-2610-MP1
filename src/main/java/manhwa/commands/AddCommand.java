package manhwa.commands;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Status;
import manhwa.Storage;

/**
 * Starts the interactive flow for adding a manhwa.
 */
public class AddCommand extends Command {
    public static final String COMMAND_WORD = "add";

    private static final String INVALID_ADD_MESSAGE =
            "Invalid add command. Expected format: add <title>.";
    private static final String INVALID_TITLE_MESSAGE = "Title cannot contain '|'.";
    private static final String DUPLICATE_TITLE_PREFIX = "A title like '";
    private static final String DUPLICATE_TITLE_SUFFIX = "' already exists.";

    private final String title;

    /**
     * Creates a command for the supplied title.
     *
     * @param title title to add
     */
    public AddCommand(String title) {
        assert title != null;
        this.title = title.trim();
    }

    /**
     * Validates the title and starts the status prompt without changing stored data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return prompt asking for the new entry's status
     * @throws ManhwaTrackerException if the title is invalid or already exists
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        validateTitle();
        if (containsTitle(list)) {
            throw new ManhwaTrackerException(
                    DUPLICATE_TITLE_PREFIX + title + DUPLICATE_TITLE_SUFFIX);
        }

        controller.startAddFlow(new Manhwa(title, Status.WISHLIST));
        return "Is \"" + title + "\" wishlist, ongoing, or completed?";
    }

    private void validateTitle() throws ManhwaTrackerException {
        if (title.isEmpty()) {
            throw new ManhwaTrackerException(INVALID_ADD_MESSAGE);
        }
        if (title.contains("|")) {
            throw new ManhwaTrackerException(INVALID_TITLE_MESSAGE);
        }
    }

    private boolean containsTitle(ManhwaList list) throws ManhwaTrackerException {
        for (int index = 1; index <= list.size(); index++) {
            if (list.get(index).getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }
}
