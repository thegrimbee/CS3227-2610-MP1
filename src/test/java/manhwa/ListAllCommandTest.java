package manhwa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ListAllCommandTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    @TempDir
    Path tempDirectory;

    @Test
    void listAll_multipleEntries_showsEveryFieldAndScoreBreakdown() throws Exception {
        Manhwa solo = new Manhwa("Solo Leveling", Status.ONGOING);
        solo.setChapters(42, 200);
        solo.addTag("action");
        solo.addTag("fantasy");
        solo.setNote("Fast-paced dungeon story.");
        solo.setRating(Aspect.PLOT, 9);
        solo.setRating(Aspect.ART, 8);
        solo.setRating(Aspect.UNIQUENESS, 7);
        solo.setRating(Aspect.CHARACTERS, 6);
        solo.setRating(Aspect.PACING, 5);
        Manhwa tower = new Manhwa("Tower of God", Status.WISHLIST);
        ManhwaList list = listWith(solo, tower);
        PreferenceProfile profile = profileWithWeights(5, 4, 3, 2, 1);
        ManhwaTracker tracker = createTracker(list, profile);

        String response = tracker.getResponse("listall");

        assertEquals(
                "Here are the full details of every entry in your list:" + LINE_SEPARATOR
                        + "1. Solo Leveling" + LINE_SEPARATOR
                        + "   Status: ongoing" + LINE_SEPARATOR
                        + "   Date added: " + solo.getDateAdded() + LINE_SEPARATOR
                        + "   Chapters: ch. 42/200" + LINE_SEPARATOR
                        + "   Tags: #action #fantasy" + LINE_SEPARATOR
                        + "   Note: Fast-paced dungeon story." + LINE_SEPARATOR
                        + "   Score breakdown:" + LINE_SEPARATOR
                        + "      plot: rating 9, weight 5, contribution 45" + LINE_SEPARATOR
                        + "      art: rating 8, weight 4, contribution 32" + LINE_SEPARATOR
                        + "      uniqueness: rating 7, weight 3, contribution 21"
                        + LINE_SEPARATOR
                        + "      characters: rating 6, weight 2, contribution 12"
                        + LINE_SEPARATOR
                        + "      pacing: rating 5, weight 1, contribution 5" + LINE_SEPARATOR
                        + "   Overall score: 115 / 15 = 7.7" + LINE_SEPARATOR
                        + LINE_SEPARATOR
                        + "2. Tower of God" + LINE_SEPARATOR
                        + "   Status: wishlist" + LINE_SEPARATOR
                        + "   Date added: " + tower.getDateAdded() + LINE_SEPARATOR
                        + "   Chapters: -/-" + LINE_SEPARATOR
                        + "   Tags: -" + LINE_SEPARATOR
                        + "   Note: -" + LINE_SEPARATOR
                        + "   Score breakdown:" + LINE_SEPARATOR
                        + "      plot: rating -, weight 5, contribution -" + LINE_SEPARATOR
                        + "      art: rating -, weight 4, contribution -" + LINE_SEPARATOR
                        + "      uniqueness: rating -, weight 3, contribution -"
                        + LINE_SEPARATOR
                        + "      characters: rating -, weight 2, contribution -"
                        + LINE_SEPARATOR
                        + "      pacing: rating -, weight 1, contribution -" + LINE_SEPARATOR
                        + "   Overall score: -",
                response);
    }

    @Test
    void listAll_missingProfile_marksWeightsContributionsAndOverallScoreUnavailable()
            throws Exception {
        Manhwa manhwa = new Manhwa("Omniscient Reader", Status.COMPLETED);
        manhwa.setRating(Aspect.PLOT, 10);
        ManhwaTracker tracker = createTracker(listWith(manhwa), null);

        String response = tracker.getResponse("listall");

        assertTrue(response.contains("plot: rating 10, weight -, contribution -"));
        assertTrue(response.endsWith("Overall score: -"));
    }

    @Test
    void listAll_emptyList_returnsEmptyListMessage() {
        ManhwaTracker tracker = createTracker(new ManhwaList(), new PreferenceProfile());

        assertEquals("Your list is empty.", tracker.getResponse("listall"));
    }

    private ManhwaTracker createTracker(ManhwaList list, PreferenceProfile profile) {
        return new ManhwaTracker(
                list, profile, new Storage(tempDirectory.toString()));
    }

    private static ManhwaList listWith(Manhwa... entries) throws ManhwaTrackerException {
        ManhwaList list = new ManhwaList();
        for (Manhwa entry : entries) {
            list.add(entry);
        }
        return list;
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
