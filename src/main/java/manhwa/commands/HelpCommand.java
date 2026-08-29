package manhwa.commands;

import java.util.StringJoiner;

import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.PreferenceProfile;
import manhwa.SortKey;
import manhwa.Status;
import manhwa.Storage;

/**
 * Lists every currently implemented command and its accepted format.
 */
public class HelpCommand extends Command {
    public static final String COMMAND_WORD = "help";

    private static final String OPTION_SEPARATOR = "|";
    private static final String OPTION_PREFIX = "<";
    private static final String OPTION_SUFFIX = ">";

    /**
     * Returns one format line for each currently implemented command.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return command formats separated by line breaks
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) {
        assert list != null;
        assert storage != null;
        assert controller != null;
        return String.join(System.lineSeparator(),
                OnboardCommand.COMMAND_WORD,
                AddCommand.COMMAND_WORD + " <title>",
                ListCommand.COMMAND_WORD + " [status]",
                FindCommand.COMMAND_WORD + " <keyword>",
                FilterCommand.COMMAND_WORD + " <tag>",
                SortCommand.COMMAND_WORD + " " + getSortKeyOptions(),
                DeleteCommand.COMMAND_WORD + " <index>",
                StatusCommand.COMMAND_WORD + " <index> " + getStatusOptions(),
                RateCommand.COMMAND_WORD + " <index> <aspect> <1-10>",
                ChapterCommand.COMMAND_WORD + " <index> <n> /of <m>",
                TagCommand.COMMAND_WORD + " <index> <tag>",
                UntagCommand.COMMAND_WORD + " <index> <tag>",
                NoteCommand.COMMAND_WORD + " <index> [<text>|" + NoteCommand.CLEAR_WORD + "]",
                StatsCommand.COMMAND_WORD,
                RerankCommand.COMMAND_WORD,
                COMMAND_WORD,
                ByeCommand.COMMAND_WORD,
                CancelCommand.COMMAND_WORD);
    }

    private String getSortKeyOptions() {
        StringJoiner options = new StringJoiner(
                OPTION_SEPARATOR, OPTION_PREFIX, OPTION_SUFFIX);
        for (SortKey sortKey : SortKey.values()) {
            options.add(sortKey.getDisplayName());
        }
        return options.toString();
    }

    private String getStatusOptions() {
        StringJoiner options = new StringJoiner(
                OPTION_SEPARATOR, OPTION_PREFIX, OPTION_SUFFIX);
        for (Status status : Status.values()) {
            options.add(status.getDisplayName());
        }
        return options.toString();
    }
}
