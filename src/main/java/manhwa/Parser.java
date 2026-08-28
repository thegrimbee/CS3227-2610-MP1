package manhwa;

import manhwa.commands.AddCommand;
import manhwa.commands.ByeCommand;
import manhwa.commands.CancelCommand;
import manhwa.commands.Command;
import manhwa.commands.DeleteCommand;
import manhwa.commands.FindCommand;
import manhwa.commands.ListCommand;
import manhwa.commands.OnboardCommand;
import manhwa.commands.RerankCommand;

/**
 * Parses user input into validated command objects.
 */
public final class Parser {
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Unknown command. Type `help` to see available commands.";
    private static final String INVALID_LIST_MESSAGE =
            "Invalid list command. Expected format: list [status].";
    private static final String INVALID_DELETE_MESSAGE =
            "Invalid delete command. Expected format: delete <index>.";
    private static final String INVALID_FIND_MESSAGE =
            "Invalid find command. Expected format: find <keyword>.";
    private static final String INVALID_ADD_MESSAGE =
            "Invalid add command. Expected format: add <title>.";
    private static final String INVALID_CANCEL_MESSAGE =
            "Invalid cancel command. Expected format: cancel.";
    private static final String INVALID_ONBOARD_MESSAGE =
            "Invalid onboard command. Expected format: onboard.";
    private static final String INVALID_RERANK_MESSAGE =
            "Invalid rerank command. Expected format: rerank.";

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
        case AddCommand.COMMAND_WORD -> parseAddCommand(input);
        case ByeCommand.COMMAND_WORD -> new ByeCommand();
        case CancelCommand.COMMAND_WORD -> parseCancelCommand(input);
        case ListCommand.COMMAND_WORD -> parseListCommand(input);
        case DeleteCommand.COMMAND_WORD -> parseDeleteCommand(input);
        case FindCommand.COMMAND_WORD -> parseFindCommand(input);
        case OnboardCommand.COMMAND_WORD -> parseOnboardCommand(input);
        case RerankCommand.COMMAND_WORD -> parseRerankCommand(input);
        default -> throw new ManhwaTrackerException(UNKNOWN_COMMAND_MESSAGE);
        };
    }

    private static Command parseAddCommand(String input) throws ManhwaTrackerException {
        String title = getArguments(input);
        if (title.isEmpty()) {
            throw new ManhwaTrackerException(INVALID_ADD_MESSAGE);
        }
        return new AddCommand(title);
    }

    private static Command parseCancelCommand(String input) throws ManhwaTrackerException {
        if (!getArguments(input).isEmpty()) {
            throw new ManhwaTrackerException(INVALID_CANCEL_MESSAGE);
        }
        return new CancelCommand();
    }

    private static Command parseOnboardCommand(String input) throws ManhwaTrackerException {
        validateNoArguments(input, INVALID_ONBOARD_MESSAGE);
        return new OnboardCommand();
    }

    private static Command parseRerankCommand(String input) throws ManhwaTrackerException {
        validateNoArguments(input, INVALID_RERANK_MESSAGE);
        return new RerankCommand();
    }

    private static Command parseListCommand(String input) throws ManhwaTrackerException {
        String arguments = getArguments(input);
        if (arguments.isEmpty()) {
            return new ListCommand();
        }
        if (arguments.split("\\s+").length != 1) {
            throw new ManhwaTrackerException(INVALID_LIST_MESSAGE);
        }
        return new ListCommand(Status.fromString(arguments));
    }

    private static Command parseDeleteCommand(String input) throws ManhwaTrackerException {
        String arguments = getArguments(input);
        if (arguments.isEmpty() || arguments.split("\\s+").length != 1) {
            throw new ManhwaTrackerException(INVALID_DELETE_MESSAGE);
        }
        try {
            return new DeleteCommand(Integer.parseInt(arguments));
        } catch (NumberFormatException exception) {
            throw new ManhwaTrackerException(INVALID_DELETE_MESSAGE);
        }
    }

    private static Command parseFindCommand(String input) throws ManhwaTrackerException {
        String keyword = getArguments(input);
        if (keyword.isEmpty()) {
            throw new ManhwaTrackerException(INVALID_FIND_MESSAGE);
        }
        return new FindCommand(keyword);
    }

    private static String getFirstWord(String input) {
        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            return "";
        }
        return trimmedInput.split("\\s+")[0];
    }

    private static String getArguments(String input) {
        String[] commandParts = input.trim().split("\\s+", 2);
        if (commandParts.length < 2) {
            return "";
        }
        return commandParts[1].trim();
    }

    private static void validateNoArguments(String input, String errorMessage)
            throws ManhwaTrackerException {
        assert errorMessage != null;
        if (!getArguments(input).isEmpty()) {
            throw new ManhwaTrackerException(errorMessage);
        }
    }
}
