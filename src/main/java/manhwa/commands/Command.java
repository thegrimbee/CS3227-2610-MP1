package manhwa.commands;

import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Represents a user command that can be executed by ManhwaDex Lite.
 */
public abstract class Command {
    private final boolean isExit;

    /**
     * Creates a non-exiting command.
     */
    protected Command() {
        this(false);
    }

    /**
     * Creates a command with the specified exit behavior.
     *
     * @param isExit whether executing this command should exit the application
     */
    protected Command(boolean isExit) {
        this.isExit = isExit;
    }

    /**
     * Reports whether this command exits the application.
     *
     * @return {@code true} when the application should exit after this command
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * Executes this command against the application's data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return message to show the user
     * @throws ManhwaTrackerException if the command cannot be executed
     */
    public abstract String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException;
}
