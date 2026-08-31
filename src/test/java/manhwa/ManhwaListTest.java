package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ManhwaListTest {
    @Test
    void sortKeyFromString_validKeys_areCaseInsensitive() throws ManhwaTrackerException {
        assertAll(
                () -> assertEquals(SortKey.SCORE, SortKey.fromString("ScOrE")),
                () -> assertEquals(SortKey.TITLE, SortKey.fromString("title")),
                () -> assertEquals(SortKey.DATE, SortKey.fromString("date")),
                () -> assertEquals(SortKey.CHAPTERS, SortKey.fromString("chapters")),
                () -> assertEquals(SortKey.PLOT, SortKey.fromString("plot")),
                () -> assertEquals(SortKey.ART, SortKey.fromString("art")),
                () -> assertEquals(SortKey.UNIQUENESS, SortKey.fromString("uniqueness")),
                () -> assertEquals(SortKey.CHARACTERS, SortKey.fromString("characters")),
                () -> assertEquals(SortKey.PACING, SortKey.fromString("pacing")));
    }

    @Test
    void sortKeyFromString_invalidKey_throwsExceptionWithExactMessage() {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> SortKey.fromString("status"));

        assertEquals(
                "Unknown sort key: status. Valid: score, title, date, chapters, plot, art, "
                        + "uniqueness, characters, pacing.",
                exception.getMessage());
    }

    @Test
    void add_caseVariantDuplicate_throwsExceptionWithExactMessage()
            throws ManhwaTrackerException {
        ManhwaList list = new ManhwaList();
        list.add(new Manhwa("Solo Leveling", Status.ONGOING));

        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class,
                () -> list.add(new Manhwa("sOlO lEvElInG", Status.COMPLETED)));

        assertAll(
                () -> assertEquals(
                        "A title like 'sOlO lEvElInG' already exists.", exception.getMessage()),
                () -> assertEquals(1, list.size()));
    }

    @Test
    void getAndDelete_useOneBasedIndexes() throws ManhwaTrackerException {
        Manhwa first = new Manhwa("First", Status.WISHLIST);
        Manhwa second = new Manhwa("Second", Status.ONGOING);
        ManhwaList list = listOf(first, second);

        assertSame(first, list.get(1));
        assertSame(first, list.delete(1));
        assertAll(
                () -> assertEquals(1, list.size()),
                () -> assertSame(second, list.get(1)));
    }

    @Test
    void getIndexOf_returnsPermanentIndexOrZeroWhenAbsent()
            throws ManhwaTrackerException {
        Manhwa first = new Manhwa("First", Status.WISHLIST);
        Manhwa second = new Manhwa("Second", Status.ONGOING);
        Manhwa absent = new Manhwa("Absent", Status.COMPLETED);
        ManhwaList list = listOf(first, second);

        assertAll(
                () -> assertEquals(1, list.getIndexOf(first)),
                () -> assertEquals(2, list.getIndexOf(second)),
                () -> assertEquals(0, list.getIndexOf(absent)));
    }

    @Test
    void getAndDelete_outOfRange_throwExceptionWithExactMessage()
            throws ManhwaTrackerException {
        ManhwaList list = listOf(new Manhwa("Only", Status.WISHLIST));

        ManhwaTrackerException zeroIndex = assertThrows(
                ManhwaTrackerException.class, () -> list.get(0));
        ManhwaTrackerException largeIndex = assertThrows(
                ManhwaTrackerException.class, () -> list.delete(2));

        assertAll(
                () -> assertEquals("Entry 0 does not exist.", zeroIndex.getMessage()),
                () -> assertEquals("Entry 2 does not exist.", largeIndex.getMessage()));
    }

    @Test
    void findByKeyword_matchesTitleSubstringCaseInsensitivelyWithoutMutation()
            throws ManhwaTrackerException {
        Manhwa solo = new Manhwa("Solo Leveling", Status.ONGOING);
        Manhwa max = new Manhwa("The Max-Level Player", Status.ONGOING);
        Manhwa tower = new Manhwa("Tower of God", Status.WISHLIST);
        ManhwaList list = listOf(solo, max, tower);

        List<Manhwa> matches = list.findByKeyword("LEVEL");

        assertAll(
                () -> assertEquals(List.of(solo, max), matches),
                () -> assertNotSame(matches, list.findByKeyword("LEVEL")),
                () -> assertStoredOrder(list, solo, max, tower));
    }

    @Test
    void filterByStatus_returnsNewMatchingListWithoutMutation()
            throws ManhwaTrackerException {
        Manhwa ongoing = new Manhwa("Ongoing", Status.ONGOING);
        Manhwa wishlist = new Manhwa("Wishlist", Status.WISHLIST);
        Manhwa completed = new Manhwa("Completed", Status.COMPLETED);
        ManhwaList list = listOf(ongoing, wishlist, completed);

        List<Manhwa> matches = list.filterByStatus(Status.ONGOING);

        assertAll(
                () -> assertEquals(List.of(ongoing), matches),
                () -> assertNotSame(matches, list.filterByStatus(Status.ONGOING)),
                () -> assertStoredOrder(list, ongoing, wishlist, completed));
    }

    @Test
    void filterByTag_returnsNewMatchingListWithoutMutation() throws ManhwaTrackerException {
        Manhwa action = new Manhwa("Action", Status.ONGOING);
        action.addTag("action");
        Manhwa mixed = new Manhwa("Mixed", Status.COMPLETED);
        mixed.addTag("action");
        mixed.addTag("romance");
        Manhwa romance = new Manhwa("Romance", Status.WISHLIST);
        romance.addTag("romance");
        ManhwaList list = listOf(action, mixed, romance);

        List<Manhwa> matches = list.filterByTag("action");

        assertAll(
                () -> assertEquals(List.of(action, mixed), matches),
                () -> assertNotSame(matches, list.filterByTag("action")),
                () -> assertStoredOrder(list, action, mixed, romance));
    }

    @Test
    void sortedView_scoreSortsDescendingWithUnratedLastWithoutMutation()
            throws ManhwaTrackerException {
        Manhwa low = ratedManhwa("Low", Aspect.PLOT, 3);
        Manhwa unrated = new Manhwa("Unrated", Status.WISHLIST);
        Manhwa high = ratedManhwa("High", Aspect.PLOT, 9);
        ManhwaList list = listOf(low, unrated, high);

        List<Manhwa> sorted = list.sortedView(SortKey.SCORE, new PreferenceProfile());

        assertAll(
                () -> assertEquals(List.of(high, low, unrated), sorted),
                () -> assertStoredOrder(list, low, unrated, high));
    }

    @Test
    void sortedView_titleSortsCaseInsensitivelyWithoutMutation()
            throws ManhwaTrackerException {
        Manhwa charlie = new Manhwa("charlie", Status.WISHLIST);
        Manhwa alpha = new Manhwa("Alpha", Status.WISHLIST);
        Manhwa bravo = new Manhwa("BRAVO", Status.WISHLIST);
        ManhwaList list = listOf(charlie, alpha, bravo);

        List<Manhwa> sorted = list.sortedView(SortKey.TITLE, new PreferenceProfile());

        assertAll(
                () -> assertEquals(List.of(alpha, bravo, charlie), sorted),
                () -> assertStoredOrder(list, charlie, alpha, bravo));
    }

    @Test
    void sortedView_dateSortUsesDescendingDatesAndDoesNotMutate()
            throws ManhwaTrackerException {
        Manhwa first = new Manhwa("First", Status.WISHLIST);
        Manhwa second = new Manhwa("Second", Status.WISHLIST);
        ManhwaList list = listOf(first, second);

        List<Manhwa> sorted = list.sortedView(SortKey.DATE, new PreferenceProfile());

        assertAll(
                () -> assertEquals(List.of(first, second), sorted),
                () -> assertEquals(first.getDateAdded(), second.getDateAdded()),
                () -> assertStoredOrder(list, first, second));
    }

    @Test
    void sortedView_chaptersSortsCurrentChapterDescendingWithoutMutation()
            throws ManhwaTrackerException {
        Manhwa low = chapterManhwa("Low", 4);
        Manhwa high = chapterManhwa("High", 20);
        Manhwa middle = chapterManhwa("Middle", 10);
        ManhwaList list = listOf(low, high, middle);

        List<Manhwa> sorted = list.sortedView(SortKey.CHAPTERS, new PreferenceProfile());

        assertAll(
                () -> assertEquals(List.of(high, middle, low), sorted),
                () -> assertStoredOrder(list, low, high, middle));
    }

    @Test
    void sortedView_everyAspectSortsDescendingWithUnratedLast()
            throws ManhwaTrackerException {
        Map<SortKey, Aspect> aspectSorts = Map.of(
                SortKey.PLOT, Aspect.PLOT,
                SortKey.ART, Aspect.ART,
                SortKey.UNIQUENESS, Aspect.UNIQUENESS,
                SortKey.CHARACTERS, Aspect.CHARACTERS,
                SortKey.PACING, Aspect.PACING);

        for (Map.Entry<SortKey, Aspect> aspectSort : aspectSorts.entrySet()) {
            Manhwa low = ratedManhwa("Low", aspectSort.getValue(), 2);
            Manhwa high = ratedManhwa("High", aspectSort.getValue(), 10);
            Manhwa unrated = new Manhwa("Unrated", Status.WISHLIST);
            ManhwaList list = listOf(low, high, unrated);

            assertEquals(
                    List.of(high, low, unrated),
                    list.sortedView(aspectSort.getKey(), new PreferenceProfile()),
                    aspectSort.getKey().getDisplayName());
        }
    }

    private static ManhwaList listOf(Manhwa... entries) throws ManhwaTrackerException {
        ManhwaList list = new ManhwaList();
        for (Manhwa entry : entries) {
            list.add(entry);
        }
        return list;
    }

    private static Manhwa ratedManhwa(String title, Aspect aspect, int rating)
            throws ManhwaTrackerException {
        Manhwa manhwa = new Manhwa(title, Status.ONGOING);
        manhwa.setRating(aspect, rating);
        return manhwa;
    }

    private static Manhwa chapterManhwa(String title, int currentChapter)
            throws ManhwaTrackerException {
        Manhwa manhwa = new Manhwa(title, Status.ONGOING);
        manhwa.setChapters(currentChapter, 0);
        return manhwa;
    }

    private static void assertStoredOrder(ManhwaList list, Manhwa... expected)
            throws ManhwaTrackerException {
        assertEquals(expected.length, list.size());
        for (int index = 0; index < expected.length; index++) {
            assertSame(expected[index], list.get(index + 1));
        }
    }
}
