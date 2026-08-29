package manhwa.commands;

import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Shows, sets, or clears the free-text note for a stored manhwa.
 */
public class NoteCommand extends Command {
    public static final String COMMAND_WORD = "note";
    public static final String CLEAR_WORD = "clear";

    private static final String NO_NOTE_PREFIX = "No note for \"";
    private static final String NOTE_SAVED_PREFIX = "Note saved for \"";
    private static final String NOTE_CLEARED_PREFIX = "Cleared note for \"";
    private static final String MESSAGE_SUFFIX = "\".";

    private final int index;
    private final String note;

    /**
     * Creates a command that shows the note for a 1-based entry index.
     *
     * @param index entry index
     */
    public NoteCommand(int index) {
        this.index = index;
        this.note = null;
    }

    /**
     * Creates a command that sets a note, or clears it when the text is {@code clear}.
     *
     * @param index entry index
     * @param note note text or {@code clear}
     */
    public NoteCommand(int index, String note) {
        assert note != null;
        this.index = index;
        this.note = note;
    }

    /**
     * Shows, sets, or clears the selected note and persists mutations.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return the note or an operation confirmation
     * @throws ManhwaTrackerException if the index or note is invalid
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        Manhwa manhwa = list.get(index);
        if (note == null) {
            return getNoteResponse(manhwa);
        }
        if (CLEAR_WORD.equals(note)) {
            return clearNote(manhwa, list, profile, storage);
        }
        return saveNote(manhwa, list, profile, storage);
    }

    private String getNoteResponse(Manhwa manhwa) {
        assert manhwa != null;
        if (manhwa.getNote() == null) {
            return NO_NOTE_PREFIX + manhwa.getTitle() + MESSAGE_SUFFIX;
        }
        return manhwa.getNote();
    }

    private String saveNote(Manhwa manhwa, ManhwaList list,
            PreferenceProfile profile, Storage storage) throws ManhwaTrackerException {
        assert manhwa != null;
        manhwa.setNote(note);
        storage.saveData(list, profile);
        return NOTE_SAVED_PREFIX + manhwa.getTitle() + MESSAGE_SUFFIX;
    }

    private String clearNote(Manhwa manhwa, ManhwaList list,
            PreferenceProfile profile, Storage storage) throws ManhwaTrackerException {
        assert manhwa != null;
        manhwa.setNote(null);
        storage.saveData(list, profile);
        return NOTE_CLEARED_PREFIX + manhwa.getTitle() + MESSAGE_SUFFIX;
    }
}
