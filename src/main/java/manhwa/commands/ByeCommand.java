package manhwa.commands;

import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Ends the current ManhwaDex Lite session.
 */
public class ByeCommand extends Command {
    public static final String COMMAND_WORD = "bye";

    private static final String FAREWELL_MESSAGE = "Bye. See you next time!";

    /**
     * Creates an exiting command.
     */
    public ByeCommand() {
        super(true);
    }

    /**
     * Returns the farewell message.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return the farewell message
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) {
        assert list != null;
        assert storage != null;
        assert controller != null;
        return FAREWELL_MESSAGE;
    }
}
