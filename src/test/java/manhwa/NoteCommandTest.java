package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manhwa.commands.NoteCommand;

class NoteCommandTest {
    @TempDir
    Path tempDirectory;

    @Test
    void note_setAndShow_savesAndReturnsFreeText() throws Exception {
        Manhwa manhwa = new Manhwa("Solo Leveling", Status.ONGOING);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = createTracker(manhwa, storage);

        String saveResponse = tracker.getResponse("note 1 The art carries the story");
        String showResponse = tracker.getResponse("note 1");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals("Note saved for \"Solo Leveling\".", saveResponse),
                () -> assertEquals("The art carries the story", showResponse),
                () -> assertEquals("The art carries the story", manhwa.getNote()),
                () -> assertEquals("The art carries the story", restored.getNote()));
    }

    @Test
    void note_clear_removesAndPersistsNote() throws Exception {
        Manhwa manhwa = new Manhwa("Eleceed", Status.ONGOING);
        manhwa.setNote("Fast and fun");
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = createTracker(manhwa, storage);

        String clearResponse = tracker.getResponse("note 1 clear");
        String showResponse = tracker.getResponse("note 1");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals("Cleared note for \"Eleceed\".", clearResponse),
                () -> assertEquals("No note for \"Eleceed\".", showResponse),
                () -> assertNull(manhwa.getNote()),
                () -> assertNull(restored.getNote()));
    }

    @Test
    void note_pipeCharacter_surfacesDomainValidationMessage() throws Exception {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);
        ManhwaTracker tracker = createTracker(
                manhwa, new Storage(tempDirectory.toString()));

        String response = tracker.getResponse("note 1 Good | surprising");

        assertAll(
                () -> assertEquals("Note cannot contain '|'.", response),
                () -> assertNull(manhwa.getNote()));
    }

    @Test
    void parser_noteForms_returnNoteCommands() throws ManhwaTrackerException {
        assertAll(
                () -> assertInstanceOf(NoteCommand.class, Parser.parseCommand("note 1")),
                () -> assertInstanceOf(
                        NoteCommand.class, Parser.parseCommand("note 1 Worth rereading")),
                () -> assertInstanceOf(
                        NoteCommand.class, Parser.parseCommand("note 1 clear")));
    }

    private ManhwaTracker createTracker(Manhwa manhwa, Storage storage)
            throws ManhwaTrackerException {
        assert manhwa != null;
        assert storage != null;
        ManhwaList list = new ManhwaList();
        list.add(manhwa);
        return new ManhwaTracker(list, null, storage);
    }
}
