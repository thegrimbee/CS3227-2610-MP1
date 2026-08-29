package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manhwa.commands.DeleteCommand;

class DeleteCommandTest {
    @TempDir
    Path tempDirectory;

    @Test
    void execute_validIndex_removesEntryReportsCountAndSaves() throws Exception {
        Manhwa removed = new Manhwa("Solo Leveling", Status.ONGOING);
        removed.addTag("action");
        removed.addTag("fantasy");
        removed.setChapters(143, 179);
        Manhwa remaining = new Manhwa("Tower of God", Status.WISHLIST);
        ManhwaList list = new ManhwaList();
        list.add(removed);
        list.add(remaining);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String response = new DeleteCommand(1).execute(list, null, storage, tracker);

        assertAll(
                () -> assertEquals(
                        "Noted. I've removed this entry:" + System.lineSeparator()
                                + "  [ONGOING] Solo Leveling  #action #fantasy  ch. 143/179  "
                                + "Score: -" + System.lineSeparator()
                                + "You now have 1 entries.",
                        response),
                () -> assertEquals(1, list.size()),
                () -> assertEquals("Tower of God", list.get(1).getTitle()),
                () -> assertEquals(1, storage.loadData().getManhwaList().size()),
                () -> assertEquals(
                        "Tower of God",
                        storage.loadData().getManhwaList().get(1).getTitle()));
    }

    @Test
    void execute_outOfRange_throwsExactErrorWithoutChangingCount() throws Exception {
        ManhwaList list = new ManhwaList();
        list.add(new Manhwa("Tower of God", Status.WISHLIST));
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class,
                () -> new DeleteCommand(2).execute(list, null, storage, tracker));

        assertAll(
                () -> assertEquals("Entry 2 does not exist.", exception.getMessage()),
                () -> assertEquals(1, list.size()));
    }
}
