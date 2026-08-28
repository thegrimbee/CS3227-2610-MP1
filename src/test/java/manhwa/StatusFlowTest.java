package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StatusFlowTest {
    @TempDir
    Path tempDirectory;

    @Test
    void status_wishlist_appliesImmediatelyAndSaves() throws Exception {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);
        ManhwaList list = listWith(manhwa);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String response = tracker.getResponse("status 1 WISHList");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals("Moved \"Tower of God\" to wishlist.", response),
                () -> assertEquals(Status.WISHLIST, manhwa.getStatus()),
                () -> assertEquals(Status.WISHLIST, restored.getStatus()));
    }

    @Test
    void status_ongoing_promptsOnlyUnratedAspectsThenAppliesAndSaves()
            throws Exception {
        Manhwa manhwa = new Manhwa("Omniscient Reader", Status.WISHLIST);
        manhwa.setRating(Aspect.PLOT, 8);
        manhwa.setRating(Aspect.UNIQUENESS, 9);
        ManhwaList list = listWith(manhwa);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        assertEquals(
                "Rate art (1-10) for \"Omniscient Reader\":",
                tracker.getResponse("status 1 ongoing"));
        assertEquals(Status.WISHLIST, manhwa.getStatus());
        assertEquals(
                "Rate characters (1-10) for \"Omniscient Reader\":",
                tracker.getResponse("7"));
        assertEquals(
                "Rate pacing (1-10) for \"Omniscient Reader\":",
                tracker.getResponse("6"));
        assertEquals(
                "Moved \"Omniscient Reader\" to ongoing.",
                tracker.getResponse("5"));

        Manhwa restored = storage.loadData().getManhwaList().get(1);
        assertAll(
                () -> assertEquals(ConversationState.IDLE, tracker.getState()),
                () -> assertEquals(Status.ONGOING, manhwa.getStatus()),
                () -> assertEquals(Integer.valueOf(7), manhwa.getRating(Aspect.ART)),
                () -> assertEquals(Integer.valueOf(9),
                        manhwa.getRating(Aspect.UNIQUENESS)),
                () -> assertEquals(Integer.valueOf(6),
                        restored.getRating(Aspect.CHARACTERS)),
                () -> assertEquals(Integer.valueOf(5), restored.getRating(Aspect.PACING)),
                () -> assertEquals(Status.ONGOING, restored.getStatus()));
    }

    @Test
    void status_fullyRatedEntry_appliesImmediately() throws Exception {
        Manhwa manhwa = new Manhwa("The Boxer", Status.ONGOING);
        for (Aspect aspect : Aspect.values()) {
            manhwa.setRating(aspect, 8);
        }
        ManhwaList list = listWith(manhwa);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String response = tracker.getResponse("status 1 completed");

        assertAll(
                () -> assertEquals("Moved \"The Boxer\" to completed.", response),
                () -> assertEquals(Status.COMPLETED, manhwa.getStatus()),
                () -> assertEquals(ConversationState.IDLE, tracker.getState()));
    }

    @Test
    void status_cancelledRatingsFlow_discardsPendingRatingsAndStatus()
            throws Exception {
        Manhwa manhwa = new Manhwa("Eleceed", Status.WISHLIST);
        ManhwaList list = listWith(manhwa);
        ManhwaTracker tracker = new ManhwaTracker(
                list, null, new Storage(tempDirectory.toString()));
        tracker.getResponse("status 1 ongoing");
        tracker.getResponse("8");

        String response = tracker.getResponse("cancel");

        assertAll(
                () -> assertEquals("Cancelled. Nothing was saved.", response),
                () -> assertEquals(Status.WISHLIST, manhwa.getStatus()),
                () -> assertNull(manhwa.getRating(Aspect.PLOT)));
    }

    private static ManhwaList listWith(Manhwa manhwa) throws ManhwaTrackerException {
        ManhwaList list = new ManhwaList();
        list.add(manhwa);
        return list;
    }
}
