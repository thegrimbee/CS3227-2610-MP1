package manhwa.commands;

import java.util.List;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Finds entries whose titles contain a keyword.
 */
public class FindCommand extends Command {
    public static final String COMMAND_WORD = "find";

    private static final String FIND_HEADER =
            "Here are the matching entries in your list:";
    private static final String NO_MATCH_PREFIX = "No entries found matching '";
    private static final String NO_MATCH_SUFFIX = "'.";

    private final String keyword;

    /**
     * Creates a title-search command.
     *
     * @param keyword keyword to find
     */
    public FindCommand(String keyword) {
        assert keyword != null;
        this.keyword = keyword;
    }

    /**
     * Finds and formats matching entries without changing stored data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return formatted matches or a no-match message
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) {
        assert list != null;
        assert storage != null;
        assert controller != null;
        List<Manhwa> matches = list.findByKeyword(keyword);
        if (matches.isEmpty()) {
            return NO_MATCH_PREFIX + keyword + NO_MATCH_SUFFIX;
        }
        return DisplayUtil.formatEntries(FIND_HEADER, matches, profile);
    }
}
