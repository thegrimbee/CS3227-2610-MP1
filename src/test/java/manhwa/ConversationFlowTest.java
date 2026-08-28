package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConversationFlowTest {
    @TempDir
    Path tempDirectory;

    @Test
    void addOngoing_allRatings_addsAndPersistsRatedEntry() throws Exception {
        ManhwaList list = new ManhwaList();
        PreferenceProfile profile = createProfile();
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, profile, storage);

        assertEquals(
                "Is \"Solo Leveling\" wishlist, ongoing, or completed?",
                tracker.getResponse("add Solo Leveling"));
        assertEquals(
                "Rate plot (1-10) for \"Solo Leveling\":",
                tracker.getResponse("OnGoInG"));
        assertEquals(
                "Rate art (1-10) for \"Solo Leveling\":",
                tracker.getResponse("8"));
        assertEquals(
                "Rate uniqueness (1-10) for \"Solo Leveling\":",
                tracker.getResponse("10"));
        assertEquals(
                "Rate characters (1-10) for \"Solo Leveling\":",
                tracker.getResponse("9"));
        assertEquals(
                "Rate pacing (1-10) for \"Solo Leveling\":",
                tracker.getResponse("9"));
        assertEquals(
                "Got it. I've added \"Solo Leveling\" to your ongoing list (Score: 8.9).",
                tracker.getResponse("9"));

        Manhwa saved = storage.loadData().getManhwaList().get(1);
        assertAll(
                () -> assertEquals(ConversationState.IDLE, tracker.getState()),
                () -> assertEquals(1, list.size()),
                () -> assertEquals(Status.ONGOING, list.get(1).getStatus()),
                () -> assertEquals(8.9, list.get(1).getOverallScore(profile)),
                () -> assertEquals("Solo Leveling", saved.getTitle()),
                () -> assertEquals(Integer.valueOf(9), saved.getRating(Aspect.PACING)));
    }

    @Test
    void addWishlist_validStatus_finishesWithoutRatingPrompts() throws Exception {
        ManhwaList list = new ManhwaList();
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        tracker.getResponse("add Tower of God");
        String response = tracker.getResponse("WISHList");

        assertAll(
                () -> assertEquals(
                        "Got it. I've added \"Tower of God\" to your wishlist.", response),
                () -> assertEquals(Status.WISHLIST, list.get(1).getStatus()),
                () -> assertEquals(1, storage.loadData().getManhwaList().size()));
    }

    @Test
    void invalidStatus_reasksUntilValidStatus() throws Exception {
        ManhwaList list = new ManhwaList();
        ManhwaTracker tracker = createTracker(list);

        tracker.getResponse("add Omniscient Reader");

        assertAll(
                () -> assertEquals(
                        "Please answer wishlist, ongoing, or completed.",
                        tracker.getResponse("paused")),
                () -> assertEquals(
                        "Rate plot (1-10) for \"Omniscient Reader\":",
                        tracker.getResponse("completed")),
                () -> assertEquals(0, list.size()));
    }

    @Test
    void invalidRating_reasksSameAspectUntilValidRating() {
        ManhwaTracker tracker = createTracker(new ManhwaList());
        tracker.getResponse("add Eleceed");
        tracker.getResponse("ongoing");

        String invalidText = tracker.getResponse("excellent");
        String outOfRange = tracker.getResponse("11");

        assertAll(
                () -> assertEquals(
                        "Rating must be an integer from 1 to 10. "
                                + "Rate plot (1-10) for \"Eleceed\":",
                        invalidText),
                () -> assertEquals(
                        "Rating must be an integer from 1 to 10. "
                                + "Rate plot (1-10) for \"Eleceed\":",
                        outOfRange),
                () -> assertEquals(
                        "Rate art (1-10) for \"Eleceed\":",
                        tracker.getResponse("7")));
    }

    @Test
    void cancelMidFlow_discardsPendingEntryAndSavesNothing() throws Exception {
        ManhwaList list = new ManhwaList();
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);
        tracker.getResponse("add The Boxer");
        tracker.getResponse("ongoing");
        tracker.getResponse("8");

        String response = tracker.getResponse("CaNcEl");

        assertAll(
                () -> assertEquals("Cancelled. Nothing was saved.", response),
                () -> assertEquals(ConversationState.IDLE, tracker.getState()),
                () -> assertEquals(0, list.size()),
                () -> assertEquals(0, storage.loadData().getManhwaList().size()),
                () -> assertEquals("Nothing to cancel.", tracker.getResponse("cancel")));
    }

    @Test
    void duplicateTitle_atAddAndAtFinish_isRejectedCaseInsensitively() throws Exception {
        ManhwaList list = new ManhwaList();
        list.add(new Manhwa("Existing", Status.WISHLIST));
        ManhwaTracker tracker = createTracker(list);

        assertEquals(
                "A title like 'eXiStInG' already exists.",
                tracker.getResponse("add eXiStInG"));

        assertEquals(
                "Is \"Appeared Later\" wishlist, ongoing, or completed?",
                tracker.getResponse("add Appeared Later"));
        list.add(new Manhwa("appeared later", Status.COMPLETED));

        assertAll(
                () -> assertEquals(
                        "A title like 'Appeared Later' already exists.",
                        tracker.getResponse("wishlist")),
                () -> assertEquals(ConversationState.IDLE, tracker.getState()),
                () -> assertEquals(2, list.size()));
    }

    private ManhwaTracker createTracker(ManhwaList list) {
        return new ManhwaTracker(list, null, new Storage(tempDirectory.toString()));
    }

    private static PreferenceProfile createProfile() throws ManhwaTrackerException {
        PreferenceProfile profile = new PreferenceProfile();
        profile.setWeight(Aspect.PLOT, 5);
        profile.setWeight(Aspect.ART, 4);
        return profile;
    }
}
