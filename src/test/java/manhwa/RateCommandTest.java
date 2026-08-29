package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RateCommandTest {
    @TempDir
    Path tempDirectory;

    @Test
    void rate_setAndUpdateRating_changesEntryAndPersists() throws Exception {
        Manhwa manhwa = new Manhwa("Solo Leveling", Status.ONGOING);
        ManhwaList list = listWith(manhwa);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String initialResponse = tracker.getResponse("rate 1 PLOT 8");
        String updateResponse = tracker.getResponse("rate 1 plot 10");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals("Updated: Solo Leveling - plot: 8/10", initialResponse),
                () -> assertEquals("Updated: Solo Leveling - plot: 10/10", updateResponse),
                () -> assertEquals(Integer.valueOf(10), manhwa.getRating(Aspect.PLOT)),
                () -> assertEquals(Integer.valueOf(10), restored.getRating(Aspect.PLOT)));
    }

    @Test
    void rate_invalidAspect_returnsAspectError() throws Exception {
        ManhwaTracker tracker = createTracker(listWith(
                new Manhwa("Tower of God", Status.ONGOING)));

        assertEquals(
                "Unknown aspect: story. Valid aspects: plot, art, uniqueness, "
                        + "characters, pacing.",
                tracker.getResponse("rate 1 story 7"));
    }

    @Test
    void rate_invalidValues_returnRatingError() throws Exception {
        ManhwaTracker tracker = createTracker(listWith(
                new Manhwa("Tower of God", Status.ONGOING)));

        assertAll(
                () -> assertEquals(
                        "Rating must be an integer from 1 to 10.",
                        tracker.getResponse("rate 1 plot high")),
                () -> assertEquals(
                        "Rating must be an integer from 1 to 10.",
                        tracker.getResponse("rate 1 plot 11")));
    }

    @Test
    void rate_outOfRangeIndex_returnsEntryError() throws Exception {
        ManhwaTracker tracker = createTracker(listWith(
                new Manhwa("Tower of God", Status.ONGOING)));

        assertEquals("Entry 2 does not exist.", tracker.getResponse("rate 2 art 6"));
    }

    private ManhwaTracker createTracker(ManhwaList list) {
        return new ManhwaTracker(list, null, new Storage(tempDirectory.toString()));
    }

    private static ManhwaList listWith(Manhwa manhwa) throws ManhwaTrackerException {
        ManhwaList list = new ManhwaList();
        list.add(manhwa);
        return list;
    }
}
