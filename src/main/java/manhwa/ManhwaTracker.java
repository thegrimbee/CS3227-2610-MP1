package manhwa;

import manhwa.commands.Command;

/**
 * Coordinates command parsing and execution for ManhwaDex Lite.
 */
public class ManhwaTracker {
    private static final String BLANK_INPUT_MESSAGE = "Please enter a command.";

    private final ManhwaList manhwaList;
    private PreferenceProfile profile;
    private final Storage storage;
    private boolean isExit;

    /**
     * Creates a controller using the loaded application data.
     *
     * @param manhwaList loaded manhwa entries
     * @param profile loaded preference profile, or {@code null} when absent
     * @param storage persistent storage
     */
    public ManhwaTracker(
            ManhwaList manhwaList, PreferenceProfile profile, Storage storage) {
        assert manhwaList != null;
        assert storage != null;
        this.manhwaList = manhwaList;
        this.profile = profile;
        this.storage = storage;
    }

    /**
     * Parses and executes one line of user input.
     *
     * @param input user input
     * @return the command response or user-facing validation message
     */
    public String getResponse(String input) {
        assert input != null;
        isExit = false;
        if (input.isBlank()) {
            return BLANK_INPUT_MESSAGE;
        }

        try {
            Command command = Parser.parseCommand(input);
            String response = command.execute(manhwaList, profile, storage, this);
            isExit = command.isExit();
            return response;
        } catch (ManhwaTrackerException exception) {
            return exception.getMessage();
        }
    }

    /**
     * Reports whether the last successfully executed command requested an exit.
     *
     * @return {@code true} when the last command was an exit command
     */
    public boolean isExit() {
        return isExit;
    }
}
