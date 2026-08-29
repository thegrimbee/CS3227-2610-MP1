package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChapterCommandTest {
    private static final String CHAPTER_ERROR_MESSAGE =
            "Invalid chapter: current must be at least 1, total must be 0 or higher, "
                    + "and current cannot exceed total.";

    @TempDir
    Path tempDirectory;

    @Test
    void chapter_noRecordedProgress_returnsNoChaptersMessage() throws Exception {
        ManhwaTracker tracker = createTracker(new Manhwa("Tower of God", Status.ONGOING));

        assertEquals("No chapters recorded yet.", tracker.getResponse("chapter 1"));
    }

    @Test
    void chapter_currentOnly_updatesDisplaysAndPersists() throws Exception {
        Manhwa manhwa = new Manhwa("Eleceed", Status.ONGOING);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = createTracker(manhwa, storage);

        String updateResponse = tracker.getResponse("chapter 1 12");
        String displayResponse = tracker.getResponse("chapter 1");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals(
                        "Updated progress for \"Eleceed\": ch. 12", updateResponse),
                () -> assertEquals("Current progress: ch. 12", displayResponse),
                () -> assertEquals(12, manhwa.getCurrentChapter()),
                () -> assertEquals(0, manhwa.getTotalChapter()),
                () -> assertEquals(12, restored.getCurrentChapter()),
                () -> assertEquals(0, restored.getTotalChapter()));
    }

    @Test
    void chapter_currentAndTotal_updatesDisplaysAndPersists() throws Exception {
        Manhwa manhwa = new Manhwa("Omniscient Reader", Status.ONGOING);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = createTracker(manhwa, storage);

        String updateResponse = tracker.getResponse("chapter 1 143 /of 179");
        String displayResponse = tracker.getResponse("chapter 1");
        String currentOnlyResponse = tracker.getResponse("chapter 1 150");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals(
                        "Updated progress for \"Omniscient Reader\": ch. 143/179",
                        updateResponse),
                () -> assertEquals("Current progress: ch. 143/179", displayResponse),
                () -> assertEquals(
                        "Updated progress for \"Omniscient Reader\": ch. 150/179",
                        currentOnlyResponse),
                () -> assertEquals(150, manhwa.getCurrentChapter()),
                () -> assertEquals(179, manhwa.getTotalChapter()),
                () -> assertEquals(150, restored.getCurrentChapter()),
                () -> assertEquals(179, restored.getTotalChapter()));
    }

    @Test
    void chapter_invalidValues_surfaceManhwaValidationMessage() throws Exception {
        ManhwaTracker tracker = createTracker(new Manhwa("The Boxer", Status.ONGOING));

        assertAll(
                () -> assertEquals(
                        CHAPTER_ERROR_MESSAGE, tracker.getResponse("chapter 1 0")),
                () -> assertEquals(
                        CHAPTER_ERROR_MESSAGE,
                        tracker.getResponse("chapter 1 20 /of 10")));
    }

    @Test
    void chapter_outOfRangeIndex_returnsEntryError() throws Exception {
        ManhwaTracker tracker = createTracker(new Manhwa("The Boxer", Status.ONGOING));

        assertEquals("Entry 2 does not exist.", tracker.getResponse("chapter 2"));
    }

    private ManhwaTracker createTracker(Manhwa manhwa) throws ManhwaTrackerException {
        return createTracker(manhwa, new Storage(tempDirectory.toString()));
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
