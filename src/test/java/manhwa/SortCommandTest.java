package manhwa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SortCommandTest {
    private static final String LIST_HEADER = "Here are the entries in your list:";

    @TempDir
    Path tempDirectory;

    @Test
    void sort_everyKey_ordersEntriesWithoutChangingStoredOrder() throws Exception {
        Manhwa alpha = createRatedManhwa(
                "Alpha", LocalDate.of(2024, 1, 1), 10, 9, 1, 8, 2, 6);
        Manhwa bravo = createRatedManhwa(
                "Bravo", LocalDate.of(2024, 1, 3), 5, 5, 9, 4, 8, 3);
        Manhwa charlie = createRatedManhwa(
                "Charlie", LocalDate.of(2024, 1, 2), 20, 1, 5, 9, 4, 8);
        Manhwa unrated = new Manhwa(
                "Delta", Status.WISHLIST, LocalDate.of(2023, 12, 31));
        ManhwaList list = listWith(alpha, bravo, charlie, unrated);
        ManhwaTracker tracker = createTracker(list);
        Map<SortKey, List<String>> expectedOrders = expectedOrders();

        for (Map.Entry<SortKey, List<String>> expected : expectedOrders.entrySet()) {
            String response = tracker.getResponse(
                    "sort " + mixedCase(expected.getKey().getDisplayName()));
            assertDisplayedOrder(response, expected.getValue());
        }

        assertEquals(4, list.size());
        assertSame(alpha, list.get(1));
        assertSame(bravo, list.get(2));
        assertSame(charlie, list.get(3));
        assertSame(unrated, list.get(4));
    }

    @Test
    void sort_scoreAndAspect_putUnratedEntryLast() throws Exception {
        Manhwa rated = createRatedManhwa(
                "Rated", LocalDate.of(2024, 1, 1), 1, 5, 5, 5, 5, 5);
        Manhwa unrated = new Manhwa("Unrated", Status.WISHLIST);
        ManhwaTracker tracker = createTracker(listWith(unrated, rated));

        for (String key : List.of(
                "score", "plot", "art", "uniqueness", "characters", "pacing")) {
            String response = tracker.getResponse("sort " + key);
            List<String> lines = response.lines().toList();
            assertTrue(lines.get(1).startsWith("2. "), key);
            assertTrue(lines.get(1).contains("] Rated"), key);
            assertTrue(lines.get(2).startsWith("1. "), key);
            assertTrue(lines.get(2).contains("] Unrated"), key);
        }
    }

    @Test
    void sort_dateAfterRestart_usesPersistedDatesAndPermanentIndices() throws Exception {
        Manhwa older = new Manhwa(
                "Older", Status.COMPLETED, LocalDate.of(2023, 6, 1));
        Manhwa newer = new Manhwa(
                "Newer", Status.ONGOING, LocalDate.of(2025, 2, 10));
        Storage storage = new Storage(tempDirectory.toString());
        storage.saveData(listWith(older, newer), new PreferenceProfile());
        LoadResult reloaded = storage.loadData();
        ManhwaTracker restartedTracker = new ManhwaTracker(
                reloaded.getManhwaList(), reloaded.getPreferenceProfile(), storage);

        String response = restartedTracker.getResponse("sort date");
        List<String> lines = response.lines().toList();

        assertEquals(LocalDate.of(2023, 6, 1), reloaded.getManhwaList().get(1).getDateAdded());
        assertEquals(LocalDate.of(2025, 2, 10), reloaded.getManhwaList().get(2).getDateAdded());
        assertTrue(lines.get(1).startsWith("2. [ONGOING] Newer"));
        assertTrue(lines.get(2).startsWith("1. [COMPLETED] Older"));
    }

    @Test
    void sort_invalidKey_returnsSortKeyError() throws Exception {
        ManhwaTracker tracker = createTracker(listWith(
                new Manhwa("Tower of God", Status.WISHLIST)));

        assertEquals(
                "Unknown sort key: status. Valid: score, title, date, chapters, plot, art, "
                        + "uniqueness, characters, pacing.",
                tracker.getResponse("sort status"));
    }

    @Test
    void sort_emptyList_returnsEmptyListMessage() {
        ManhwaTracker tracker = createTracker(new ManhwaList());

        assertEquals("Your list is empty.", tracker.getResponse("sort score"));
    }

    private ManhwaTracker createTracker(ManhwaList list) {
        return new ManhwaTracker(
                list, new PreferenceProfile(), new Storage(tempDirectory.toString()));
    }

    private static Manhwa createRatedManhwa(String title, LocalDate date, int chapter,
            int plot, int art, int uniqueness, int characters, int pacing) throws Exception {
        Manhwa manhwa = new Manhwa(title, Status.COMPLETED, date);
        manhwa.setChapters(chapter, 0);
        manhwa.setRating(Aspect.PLOT, plot);
        manhwa.setRating(Aspect.ART, art);
        manhwa.setRating(Aspect.UNIQUENESS, uniqueness);
        manhwa.setRating(Aspect.CHARACTERS, characters);
        manhwa.setRating(Aspect.PACING, pacing);
        return manhwa;
    }

    private static ManhwaList listWith(Manhwa... entries) throws ManhwaTrackerException {
        ManhwaList list = new ManhwaList();
        for (Manhwa entry : entries) {
            list.add(entry);
        }
        return list;
    }

    private static Map<SortKey, List<String>> expectedOrders() {
        Map<SortKey, List<String>> expected = new EnumMap<>(SortKey.class);
        expected.put(SortKey.SCORE, List.of("Bravo", "Charlie", "Alpha", "Delta"));
        expected.put(SortKey.TITLE, List.of("Alpha", "Bravo", "Charlie", "Delta"));
        expected.put(SortKey.DATE, List.of("Bravo", "Charlie", "Alpha", "Delta"));
        expected.put(SortKey.CHAPTERS, List.of("Charlie", "Alpha", "Bravo", "Delta"));
        expected.put(SortKey.PLOT, List.of("Alpha", "Bravo", "Charlie", "Delta"));
        expected.put(SortKey.ART, List.of("Bravo", "Charlie", "Alpha", "Delta"));
        expected.put(SortKey.UNIQUENESS, List.of("Charlie", "Alpha", "Bravo", "Delta"));
        expected.put(SortKey.CHARACTERS, List.of("Bravo", "Charlie", "Alpha", "Delta"));
        expected.put(SortKey.PACING, List.of("Charlie", "Alpha", "Bravo", "Delta"));
        return expected;
    }

    private static String mixedCase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private static void assertDisplayedOrder(String response, List<String> expectedTitles) {
        List<String> lines = response.lines().toList();
        assertEquals(LIST_HEADER, lines.get(0));
        assertEquals(expectedTitles.size() + 1, lines.size());
        for (int index = 0; index < expectedTitles.size(); index++) {
            String title = expectedTitles.get(index);
            String displayedLine = lines.get(index + 1);
            assertTrue(displayedLine.startsWith(getPermanentIndex(title) + ". "), displayedLine);
            assertTrue(displayedLine.contains("] " + title), displayedLine);
        }
    }

    private static int getPermanentIndex(String title) {
        return switch (title) {
        case "Alpha" -> 1;
        case "Bravo" -> 2;
        case "Charlie" -> 3;
        case "Delta" -> 4;
        default -> throw new IllegalArgumentException("Unexpected title: " + title);
        };
    }
}
