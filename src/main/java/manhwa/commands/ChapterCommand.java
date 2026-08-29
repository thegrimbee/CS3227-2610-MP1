package manhwa.commands;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Shows or updates the reading progress of a stored manhwa.
 */
public class ChapterCommand extends Command {
    public static final String COMMAND_WORD = "chapter";

    private static final String NO_PROGRESS_MESSAGE = "No chapters recorded yet.";
    private static final String CURRENT_PROGRESS_PREFIX = "Current progress: ";
    private static final String UPDATED_PROGRESS_PREFIX = "Updated progress for \"";
    private static final String UPDATED_PROGRESS_SEPARATOR = "\": ";
    private static final int NO_CHAPTER = 0;

    private final int index;
    private final Integer currentChapter;
    private final Integer totalChapter;

    /**
     * Creates a command that shows progress for a 1-based entry index.
     *
     * @param index entry index
     */
    public ChapterCommand(int index) {
        this(index, null, null);
    }

    /**
     * Creates a command that updates only the current chapter.
     *
     * @param index entry index
     * @param currentChapter current chapter
     */
    public ChapterCommand(int index, int currentChapter) {
        this(index, currentChapter, null);
    }

    /**
     * Creates a command that updates the current and total chapters.
     *
     * @param index entry index
     * @param currentChapter current chapter
     * @param totalChapter total chapters, or 0 when unknown
     */
    public ChapterCommand(int index, int currentChapter, int totalChapter) {
        this(index, Integer.valueOf(currentChapter), Integer.valueOf(totalChapter));
    }

    private ChapterCommand(int index, Integer currentChapter, Integer totalChapter) {
        this.index = index;
        this.currentChapter = currentChapter;
        this.totalChapter = totalChapter;
    }

    /**
     * Shows progress or updates and persists the selected entry.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return current progress or an update confirmation
     * @throws ManhwaTrackerException if the index or chapter values are invalid
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        Manhwa manhwa = list.get(index);
        if (currentChapter == null) {
            return getCurrentProgress(manhwa);
        }

        int updatedTotal = totalChapter == null
                ? manhwa.getTotalChapter() : totalChapter;
        manhwa.setChapters(currentChapter, updatedTotal);
        storage.saveData(list, profile);
        return UPDATED_PROGRESS_PREFIX + manhwa.getTitle()
                + UPDATED_PROGRESS_SEPARATOR + manhwa.getChapterDisplay();
    }

    private String getCurrentProgress(Manhwa manhwa) {
        assert manhwa != null;
        if (manhwa.getCurrentChapter() == NO_CHAPTER) {
            return NO_PROGRESS_MESSAGE;
        }
        return CURRENT_PROGRESS_PREFIX + manhwa.getChapterDisplay();
    }
}
