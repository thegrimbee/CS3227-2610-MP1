package manhwa;

import manhwa.commands.ByeCommand;
import manhwa.commands.Command;

/**
 * Parses user input into validated command objects.
 */
public final class Parser {
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Unknown command. Type `help` to see available commands.";

    private Parser() {
    }

    /**
     * Parses a command from the first whitespace-delimited input word.
     *
     * @param input user input to parse
     * @return the parsed command
     * @throws ManhwaTrackerException if the command word is unknown
     */
    public static Command parseCommand(String input) throws ManhwaTrackerException {
        assert input != null;
        String commandWord = getFirstWord(input);
        return switch (commandWord) {
        case ByeCommand.COMMAND_WORD -> new ByeCommand();
        default -> throw new ManhwaTrackerException(UNKNOWN_COMMAND_MESSAGE);
        };
    }

    private static String getFirstWord(String input) {
        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            return "";
        }
        return trimmedInput.split("\\s+")[0];
    }
}
