package manhwa.commands;

import java.util.List;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Displays entries carrying a selected tag.
 */
public class FilterCommand extends Command {
    public static final String COMMAND_WORD = "filter";

    private final String tag;

    /**
     * Creates a filter command for the supplied tag.
     *
     * @param tag tag to find
     */
    public FilterCommand(String tag) {
        assert tag != null;
        this.tag = tag;
    }

    /**
     * Formats matching entries without changing stored data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return numbered matches or a no-match message
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) {
        assert list != null;
        assert storage != null;
        assert controller != null;
        List<Manhwa> matches = list.filterByTag(tag);
        if (matches.isEmpty()) {
            return "No entries found with tag '#" + tag + "'.";
        }
        String header = "Here are the entries with tag '#" + tag + "':";
        return DisplayUtil.formatEntries(header, matches, profile, list);
    }
}
