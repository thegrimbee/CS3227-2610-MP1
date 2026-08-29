package manhwa;

import manhwa.commands.AddCommand;
import manhwa.commands.ByeCommand;
import manhwa.commands.CancelCommand;
import manhwa.commands.ChapterCommand;
import manhwa.commands.Command;
import manhwa.commands.DeleteCommand;
import manhwa.commands.FindCommand;
import manhwa.commands.FilterCommand;
import manhwa.commands.ListCommand;
import manhwa.commands.OnboardCommand;
import manhwa.commands.RateCommand;
import manhwa.commands.RerankCommand;
import manhwa.commands.SortCommand;
import manhwa.commands.StatusCommand;
import manhwa.commands.TagCommand;
import manhwa.commands.UntagCommand;

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
    private static final String INVALID_RATE_MESSAGE =
            "Invalid rate command. Expected format: rate <index> <aspect> <1-10>.";
    private static final String INVALID_STATUS_MESSAGE =
            "Invalid status command. Expected format: status <index> <status>.";
    private static final String INVALID_RATING_MESSAGE =
            "Rating must be an integer from 1 to 10.";
    private static final String INVALID_SORT_MESSAGE =
            "Invalid sort command. Expected format: sort <key>.";
    private static final String INVALID_TAG_MESSAGE =
            "Invalid tag command. Expected format: tag <index> <tag>.";
    private static final String INVALID_UNTAG_MESSAGE =
            "Invalid untag command. Expected format: untag <index> <tag>.";
    private static final String INVALID_FILTER_MESSAGE =
            "Invalid filter command. Expected format: filter <tag>.";
    private static final String INVALID_CHAPTER_MESSAGE =
            "Invalid chapter command. Expected format: "
                    + "chapter <index> <n> /of <m>. E.g. chapter 1 5 /of 10";
    private static final String TOTAL_CHAPTER_SEPARATOR = "/of";

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
        case ChapterCommand.COMMAND_WORD -> parseChapterCommand(input);
        case ListCommand.COMMAND_WORD -> parseListCommand(input);
        case DeleteCommand.COMMAND_WORD -> parseDeleteCommand(input);
        case FilterCommand.COMMAND_WORD -> parseFilterCommand(input);
        case FindCommand.COMMAND_WORD -> parseFindCommand(input);
        case OnboardCommand.COMMAND_WORD -> parseOnboardCommand(input);
        case RateCommand.COMMAND_WORD -> parseRateCommand(input);
        case RerankCommand.COMMAND_WORD -> parseRerankCommand(input);
        case SortCommand.COMMAND_WORD -> parseSortCommand(input);
        case StatusCommand.COMMAND_WORD -> parseStatusCommand(input);
        case TagCommand.COMMAND_WORD -> parseTagCommand(input);
        case UntagCommand.COMMAND_WORD -> parseUntagCommand(input);
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

    private static Command parseRateCommand(String input) throws ManhwaTrackerException {
        String[] arguments = getArguments(input).split("\\s+");
        if (arguments.length != 3) {
            throw new ManhwaTrackerException(INVALID_RATE_MESSAGE);
        }
        int index = parseIndex(arguments[0], INVALID_RATE_MESSAGE);
        Aspect aspect = Aspect.fromString(arguments[1]);
        try {
            return new RateCommand(index, aspect, Integer.parseInt(arguments[2]));
        } catch (NumberFormatException exception) {
            throw new ManhwaTrackerException(INVALID_RATING_MESSAGE);
        }
    }

    private static Command parseStatusCommand(String input) throws ManhwaTrackerException {
        String[] arguments = getArguments(input).split("\\s+");
        if (arguments.length != 2) {
            throw new ManhwaTrackerException(INVALID_STATUS_MESSAGE);
        }
        int index = parseIndex(arguments[0], INVALID_STATUS_MESSAGE);
        return new StatusCommand(index, Status.fromString(arguments[1]));
    }

    private static Command parseChapterCommand(String input) throws ManhwaTrackerException {
        String arguments = getArguments(input);
        String[] chapterParts = arguments.split(TOTAL_CHAPTER_SEPARATOR, -1);
        if (arguments.isEmpty() || chapterParts.length > 2) {
            throw new ManhwaTrackerException(INVALID_CHAPTER_MESSAGE);
        }

        String[] currentParts = chapterParts[0].trim().split("\\s+");
        if (chapterParts.length == 1) {
            return parseChapterWithoutTotal(currentParts);
        }
        return parseChapterWithTotal(currentParts, chapterParts[1]);
    }

    private static Command parseChapterWithoutTotal(String[] currentParts)
            throws ManhwaTrackerException {
        assert currentParts != null;
        if (currentParts.length == 1) {
            return new ChapterCommand(parseIndex(currentParts[0], INVALID_CHAPTER_MESSAGE));
        }
        if (currentParts.length == 2) {
            int index = parseIndex(currentParts[0], INVALID_CHAPTER_MESSAGE);
            int current = parseIndex(currentParts[1], INVALID_CHAPTER_MESSAGE);
            return new ChapterCommand(index, current);
        }
        throw new ManhwaTrackerException(INVALID_CHAPTER_MESSAGE);
    }

    private static Command parseChapterWithTotal(String[] currentParts, String totalPart)
            throws ManhwaTrackerException {
        assert currentParts != null;
        assert totalPart != null;
        String[] totalParts = totalPart.trim().split("\\s+");
        if (currentParts.length != 2 || totalParts.length != 1 || totalParts[0].isEmpty()) {
            throw new ManhwaTrackerException(INVALID_CHAPTER_MESSAGE);
        }
        int index = parseIndex(currentParts[0], INVALID_CHAPTER_MESSAGE);
        int current = parseIndex(currentParts[1], INVALID_CHAPTER_MESSAGE);
        int total = parseIndex(totalParts[0], INVALID_CHAPTER_MESSAGE);
        return new ChapterCommand(index, current, total);
    }

    private static Command parseSortCommand(String input) throws ManhwaTrackerException {
        String arguments = getArguments(input);
        if (arguments.isEmpty() || arguments.split("\\s+").length != 1) {
            throw new ManhwaTrackerException(INVALID_SORT_MESSAGE);
        }
        return new SortCommand(SortKey.fromString(arguments));
    }

    private static Command parseTagCommand(String input) throws ManhwaTrackerException {
        String[] arguments = getArguments(input).split("\\s+");
        if (arguments.length != 2) {
            throw new ManhwaTrackerException(INVALID_TAG_MESSAGE);
        }
        int index = parseIndex(arguments[0], INVALID_TAG_MESSAGE);
        return new TagCommand(index, arguments[1]);
    }

    private static Command parseUntagCommand(String input) throws ManhwaTrackerException {
        String[] arguments = getArguments(input).split("\\s+");
        if (arguments.length != 2) {
            throw new ManhwaTrackerException(INVALID_UNTAG_MESSAGE);
        }
        int index = parseIndex(arguments[0], INVALID_UNTAG_MESSAGE);
        return new UntagCommand(index, arguments[1]);
    }

    private static Command parseFilterCommand(String input) throws ManhwaTrackerException {
        String arguments = getArguments(input);
        if (arguments.isEmpty() || arguments.split("\\s+").length != 1) {
            throw new ManhwaTrackerException(INVALID_FILTER_MESSAGE);
        }
        return new FilterCommand(arguments);
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

    private static int parseIndex(String value, String errorMessage)
            throws ManhwaTrackerException {
        assert value != null;
        assert errorMessage != null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ManhwaTrackerException(errorMessage);
        }
    }
}
