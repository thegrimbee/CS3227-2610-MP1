package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OnboardFlowTest {
    private static final String FIRST_PROMPT =
            "Let's set up your scoring preferences! "
                    + "Rate how important each aspect is from 1 to 5."
                    + System.lineSeparator()
                    + "Importance of plot (1-5):";
    private static final String SAVED_MESSAGE =
            "Preferences saved! Overall scores will now use your priorities. "
                    + "You can change your scoring mechanism by rerunning the onboard command.";

    @TempDir
    Path tempDirectory;

    @Test
    void startup_missingStorageFile_startsOnboardingAutomatically() throws Exception {
        Storage storage = new Storage(tempDirectory.resolve("missing").toString());
        LoadResult loadResult = storage.loadData();
        ManhwaTracker tracker = new ManhwaTracker(
                loadResult.getManhwaList(), loadResult.getPreferenceProfile(), storage);

        assertAutomaticOnboarding(tracker);
    }

    @Test
    void startup_fileWithoutPreference_startsOnboardingAutomatically() throws Exception {
        Storage storage = new Storage(tempDirectory.toString());
        storage.saveData(new ManhwaList(), null);
        LoadResult loadResult = storage.loadData();
        ManhwaTracker tracker = new ManhwaTracker(
                loadResult.getManhwaList(), loadResult.getPreferenceProfile(), storage);

        assertAutomaticOnboarding(tracker);
    }

    @Test
    void startup_existingProfile_doesNotStartOnboarding() {
        ManhwaTracker tracker = createTracker(new PreferenceProfile());

        String prompt = tracker.startOnboardingIfNeeded();

        assertAll(
                () -> assertNull(prompt),
                () -> assertEquals(ConversationState.IDLE, tracker.getState()));
    }

    @Test
    void onboard_allImportanceAnswers_activatesAndPersistsProfile() throws Exception {
        Manhwa ratedManhwa = new Manhwa("Solo Leveling", Status.COMPLETED);
        ratedManhwa.setRating(Aspect.PLOT, 8);
        ratedManhwa.setRating(Aspect.ART, 10);
        ManhwaList list = new ManhwaList();
        list.add(ratedManhwa);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        assertTrue(tracker.getResponse("list").endsWith("Score: \u2014"));
        assertEquals(FIRST_PROMPT, tracker.getResponse("onboard"));
        assertEquals("Importance of art (1-5):", tracker.getResponse("5"));
        assertEquals("Importance of uniqueness (1-5):", tracker.getResponse("4"));
        assertEquals("Importance of characters (1-5):", tracker.getResponse("3"));
        assertEquals("Importance of pacing (1-5):", tracker.getResponse("2"));
        assertEquals(SAVED_MESSAGE, tracker.getResponse("1"));

        PreferenceProfile restored = storage.loadData().getPreferenceProfile();
        assertAll(
                () -> assertEquals(ConversationState.IDLE, tracker.getState()),
                () -> assertTrue(tracker.getResponse("list").endsWith("Score: 8.9")),
                () -> assertEquals(5, restored.getWeight(Aspect.PLOT)),
                () -> assertEquals(4, restored.getWeight(Aspect.ART)),
                () -> assertEquals(3, restored.getWeight(Aspect.UNIQUENESS)),
                () -> assertEquals(2, restored.getWeight(Aspect.CHARACTERS)),
                () -> assertEquals(1, restored.getWeight(Aspect.PACING)));
    }

    @Test
    void onboard_invalidImportance_reasksSameAspectUntilValid() {
        ManhwaTracker tracker = createTracker(null);
        tracker.getResponse("onboard");

        String invalidText = tracker.getResponse("high");
        String belowRange = tracker.getResponse("0");
        String aboveRange = tracker.getResponse("6");

        assertAll(
                () -> assertEquals(
                        "Importance must be an integer from 1 to 5. "
                                + "Importance of plot (1-5):",
                        invalidText),
                () -> assertEquals(
                        "Importance must be an integer from 1 to 5. "
                                + "Importance of plot (1-5):",
                        belowRange),
                () -> assertEquals(
                        "Importance must be an integer from 1 to 5. "
                                + "Importance of plot (1-5):",
                        aboveRange),
                () -> assertEquals(
                        "Importance of art (1-5):", tracker.getResponse("3")));
    }

    @Test
    void cancelMidOnboarding_leavesExistingProfileUnchanged() throws Exception {
        PreferenceProfile original = profileWithWeights(1, 2, 3, 4, 5);
        Storage storage = new Storage(tempDirectory.toString());
        storage.saveData(new ManhwaList(), original);
        ManhwaTracker tracker = new ManhwaTracker(new ManhwaList(), original, storage);
        tracker.getResponse("onboard");
        tracker.getResponse("5");
        tracker.getResponse("4");

        String response = tracker.getResponse("CANCEL");
        PreferenceProfile restored = storage.loadData().getPreferenceProfile();

        assertAll(
                () -> assertEquals("Cancelled. Nothing was saved.", response),
                () -> assertEquals(ConversationState.IDLE, tracker.getState()),
                () -> assertEquals(1, restored.getWeight(Aspect.PLOT)),
                () -> assertEquals(2, restored.getWeight(Aspect.ART)),
                () -> assertEquals(3, restored.getWeight(Aspect.UNIQUENESS)),
                () -> assertEquals(4, restored.getWeight(Aspect.CHARACTERS)),
                () -> assertEquals(5, restored.getWeight(Aspect.PACING)));
    }

    @Test
    void rerank_allImportanceAnswers_overwritesPreviousWeights() throws Exception {
        PreferenceProfile original = profileWithWeights(1, 1, 1, 1, 1);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(new ManhwaList(), original, storage);

        assertEquals(FIRST_PROMPT, tracker.getResponse("rerank"));
        tracker.getResponse("5");
        tracker.getResponse("4");
        tracker.getResponse("3");
        tracker.getResponse("2");
        assertEquals(SAVED_MESSAGE, tracker.getResponse("1"));

        PreferenceProfile restored = storage.loadData().getPreferenceProfile();
        assertAll(
                () -> assertEquals(5, restored.getWeight(Aspect.PLOT)),
                () -> assertEquals(4, restored.getWeight(Aspect.ART)),
                () -> assertEquals(3, restored.getWeight(Aspect.UNIQUENESS)),
                () -> assertEquals(2, restored.getWeight(Aspect.CHARACTERS)),
                () -> assertEquals(1, restored.getWeight(Aspect.PACING)));
    }

    private ManhwaTracker createTracker(PreferenceProfile profile) {
        return new ManhwaTracker(
                new ManhwaList(), profile, new Storage(tempDirectory.toString()));
    }

    private static void assertAutomaticOnboarding(ManhwaTracker tracker) {
        String prompt = tracker.startOnboardingIfNeeded();
        assertAll(
                () -> assertEquals(FIRST_PROMPT, prompt),
                () -> assertEquals(ConversationState.AWAITING_IMPORTANCE, tracker.getState()));
    }

    private static PreferenceProfile profileWithWeights(
            int plot, int art, int uniqueness, int characters, int pacing)
            throws ManhwaTrackerException {
        PreferenceProfile profile = new PreferenceProfile();
        profile.setWeight(Aspect.PLOT, plot);
        profile.setWeight(Aspect.ART, art);
        profile.setWeight(Aspect.UNIQUENESS, uniqueness);
        profile.setWeight(Aspect.CHARACTERS, characters);
        profile.setWeight(Aspect.PACING, pacing);
        return profile;
    }
}
