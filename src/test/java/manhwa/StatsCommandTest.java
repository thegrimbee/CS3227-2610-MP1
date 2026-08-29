package manhwa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manhwa.commands.StatsCommand;

class StatsCommandTest {
    @TempDir
    Path tempDirectory;

    @Test
    void stats_seededList_returnsAllExactAggregateLines() throws Exception {
        PreferenceProfile profile = createProfile();
        Manhwa alpha = createRatedManhwa(
                "Alpha", Status.ONGOING, 100, new int[] {8, 10, 9, 7, 6});
        alpha.addTag("action");
        alpha.addTag("fantasy");
        Manhwa beta = createRatedManhwa(
                "Beta", Status.ONGOING, 50, new int[] {6, 8, 7, 9, 10});
        beta.addTag("action");
        beta.addTag("regression");
        Manhwa gamma = new Manhwa("Gamma", Status.COMPLETED);
        gamma.setRating(Aspect.PLOT, 10);
        gamma.setRating(Aspect.ART, 6);
        gamma.setChapters(12, 0);
        gamma.addTag("action");
        gamma.addTag("fantasy");
        Manhwa unrated = new Manhwa("Unrated", Status.WISHLIST);
        unrated.addTag("fantasy");
        unrated.addTag("regression");
        ManhwaTracker tracker = createTracker(
                profile, alpha, beta, gamma, unrated);

        String response = tracker.getResponse("stats");

        assertEquals(String.join(System.lineSeparator(),
                "Total entries: 4",
                "Wishlist: 1 | Ongoing: 2 | Completed: 1",
                "Average overall score (rated): 8.1",
                "Average ratings: plot 8.0 | art 8.0 | uniqueness 8.0"
                        + " | characters 8.0 | pacing 8.0",
                "Total chapters read: 162",
                "Top 3 by score: 1. Gamma (8.5) | 2. Alpha (8.4) | 3. Beta (7.3)",
                "Top 3 tags: #action (3) | #fantasy (3) | #regression (2)",
                "Top priority aspects: plot (5) | uniqueness (4) | art (3)"),
                response);
    }

    @Test
    void stats_emptyList_returnsEmptyAggregateLines() throws Exception {
        ManhwaTracker tracker = createTracker(new PreferenceProfile());

        assertEquals(String.join(System.lineSeparator(),
                "Total entries: 0",
                "Wishlist: 0 | Ongoing: 0 | Completed: 0",
                "No rated entries yet.",
                "Average ratings:",
                "Total chapters read: 0",
                "Top 3 by score:",
                "Top 3 tags:",
                "Top priority aspects: plot (1) | art (1) | uniqueness (1)"),
                tracker.getResponse("stats"));
    }

    @Test
    void stats_nullProfile_usesDefaultScoresAndPromptsForOnboarding() throws Exception {
        Manhwa manhwa = new Manhwa("Unprofiled", Status.ONGOING);
        manhwa.setRating(Aspect.PLOT, 7);
        manhwa.setRating(Aspect.ART, 9);
        ManhwaTracker tracker = createTracker(null, manhwa);

        assertEquals(String.join(System.lineSeparator(),
                "Total entries: 1",
                "Wishlist: 0 | Ongoing: 1 | Completed: 0",
                "Average overall score (rated): 8.0",
                "Average ratings: plot 7.0 | art 9.0",
                "Total chapters read: 0",
                "Top 3 by score: 1. Unprofiled (8.0)",
                "Top 3 tags:",
                "Run `onboard` first to set your priorities."),
                tracker.getResponse("stats"));
    }

    @Test
    void parser_statsWithoutArguments_returnsStatsCommand() throws ManhwaTrackerException {
        assertInstanceOf(StatsCommand.class, Parser.parseCommand("stats"));
    }

    private PreferenceProfile createProfile() throws ManhwaTrackerException {
        PreferenceProfile profile = new PreferenceProfile();
        profile.setWeight(Aspect.PLOT, 5);
        profile.setWeight(Aspect.ART, 3);
        profile.setWeight(Aspect.UNIQUENESS, 4);
        profile.setWeight(Aspect.CHARACTERS, 2);
        profile.setWeight(Aspect.PACING, 1);
        return profile;
    }

    private Manhwa createRatedManhwa(
            String title, Status status, int chapters, int[] ratings)
            throws ManhwaTrackerException {
        assert title != null;
        assert status != null;
        assert ratings != null;
        Manhwa manhwa = new Manhwa(title, status);
        Aspect[] aspects = Aspect.values();
        assert ratings.length == aspects.length;
        for (int index = 0; index < aspects.length; index++) {
            manhwa.setRating(aspects[index], ratings[index]);
        }
        manhwa.setChapters(chapters, 0);
        return manhwa;
    }

    private ManhwaTracker createTracker(
            PreferenceProfile profile, Manhwa... entries) throws ManhwaTrackerException {
        ManhwaList list = new ManhwaList();
        for (Manhwa entry : entries) {
            list.add(entry);
        }
        return new ManhwaTracker(list, profile, new Storage(tempDirectory.toString()));
    }
}
