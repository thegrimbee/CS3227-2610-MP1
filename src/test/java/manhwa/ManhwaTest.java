package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class ManhwaTest {
    private static final String CHAPTER_ERROR_MESSAGE =
            "Invalid chapter: current must be at least 1, total must be 0 or higher, "
                    + "and current cannot exceed total.";

    @Test
    void constructor_newManhwa_hasExpectedDefaults() {
        Manhwa manhwa = new Manhwa("Tower of God", Status.WISHLIST);

        assertAll(
                () -> assertEquals("Tower of God", manhwa.getTitle()),
                () -> assertEquals(Status.WISHLIST, manhwa.getStatus()),
                () -> assertEquals(LocalDate.now(), manhwa.getDateAdded()),
                () -> assertFalse(manhwa.isRated()),
                () -> assertEquals(0, manhwa.getCurrentChapter()),
                () -> assertEquals(0, manhwa.getTotalChapter()),
                () -> assertEquals(List.of(), manhwa.getTags()),
                () -> assertNull(manhwa.getNote()),
                () -> assertEquals("-/-", manhwa.getChapterDisplay()));
    }

    @Test
    void getOverallScore_weightedRatings_roundsHalfUpToOneDecimal()
            throws ManhwaTrackerException {
        PreferenceProfile profile = new PreferenceProfile();
        profile.setWeight(Aspect.PLOT, 5);
        profile.setWeight(Aspect.ART, 4);
        Manhwa manhwa = new Manhwa("Solo Leveling", Status.ONGOING);
        manhwa.setRating(Aspect.PLOT, 8);
        manhwa.setRating(Aspect.ART, 10);

        assertEquals(8.9, manhwa.getOverallScore(profile));
    }

    @Test
    void getOverallScore_unratedManhwa_returnsNegativeOne() {
        Manhwa manhwa = new Manhwa("Tower of God", Status.WISHLIST);

        assertEquals(-1.0, manhwa.getOverallScore(new PreferenceProfile()));
    }

    @Test
    void setRating_invalidRating_throwsExceptionWithExactMessage() {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);

        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> manhwa.setRating(Aspect.PLOT, 11));

        assertEquals("Rating must be an integer from 1 to 10.", exception.getMessage());
    }

    @Test
    void setChapters_validProgress_updatesChapterDisplay() throws ManhwaTrackerException {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);

        manhwa.setChapters(143, 179);
        assertEquals("ch. 143/179", manhwa.getChapterDisplay());

        manhwa.setChapters(12, 0);
        assertEquals("ch. 12", manhwa.getChapterDisplay());
    }

    @Test
    void setChapters_invalidValues_throwExceptionWithExactMessage() {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);

        assertAll(
                () -> assertChapterError(manhwa, 0, 0),
                () -> assertChapterError(manhwa, 1, -1),
                () -> assertChapterError(manhwa, 11, 10));
    }

    @Test
    void addTag_invalidTags_throwExceptionWithExactMessage() {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);

        assertAll(
                () -> assertTagError(manhwa, ""),
                () -> assertTagError(manhwa, "dark fantasy"),
                () -> assertTagError(manhwa, "action,fantasy"),
                () -> assertTagError(manhwa, "action|fantasy"));
    }

    @Test
    void addTag_duplicateTag_doesNotAddDuplicate() throws ManhwaTrackerException {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);

        manhwa.addTag("action");
        manhwa.addTag("action");

        assertEquals(List.of("action"), manhwa.getTags());
    }

    @Test
    void setNote_pipeCharacter_throwsException() {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);

        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> manhwa.setNote("Good | surprising"));

        assertEquals("Note cannot contain '|'.", exception.getMessage());
    }

    @Test
    void fileString_populatedManhwa_roundTripsAllPersistedFields()
            throws ManhwaTrackerException {
        Manhwa original = new Manhwa("Solo Leveling", Status.ONGOING);
        original.addTag("action");
        original.addTag("fantasy");
        original.setRating(Aspect.PLOT, 9);
        original.setRating(Aspect.ART, 10);
        original.setChapters(143, 179);
        original.setNote("The art carries the story");

        String serialized = original.toFileString();
        Manhwa restored = Manhwa.fromFileString(serialized);

        assertAll(
                () -> assertEquals(
                        "MANHWA | Solo Leveling | ONGOING | action,fantasy | plot=9;art=10 "
                                + "| 143/179 | The art carries the story",
                        serialized),
                () -> assertEquals(original.getTitle(), restored.getTitle()),
                () -> assertEquals(original.getStatus(), restored.getStatus()),
                () -> assertEquals(original.getTags(), restored.getTags()),
                () -> assertEquals(original.getRating(Aspect.PLOT),
                        restored.getRating(Aspect.PLOT)),
                () -> assertEquals(original.getRating(Aspect.ART),
                        restored.getRating(Aspect.ART)),
                () -> assertEquals(original.getCurrentChapter(), restored.getCurrentChapter()),
                () -> assertEquals(original.getTotalChapter(), restored.getTotalChapter()),
                () -> assertEquals(original.getNote(), restored.getNote()));
    }

    @Test
    void fileString_emptyOptionalFields_roundTripsDefaults() throws ManhwaTrackerException {
        Manhwa restored = Manhwa.fromFileString(
                "MANHWA | Tower of God | WISHLIST |  |  | 0/0 | ");

        assertAll(
                () -> assertEquals(List.of(), restored.getTags()),
                () -> assertFalse(restored.isRated()),
                () -> assertEquals(0, restored.getCurrentChapter()),
                () -> assertEquals(0, restored.getTotalChapter()),
                () -> assertNull(restored.getNote()),
                () -> assertEquals(
                        "MANHWA | Tower of God | WISHLIST |  |  | 0/0 | ",
                        restored.toFileString()));
    }

    @Test
    void fromFileString_malformedLine_throwsTrackerException() {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class,
                () -> Manhwa.fromFileString("MANHWA | incomplete"));

        assertTrue(exception.getMessage().startsWith("Malformed manhwa data:"));
    }

    private static void assertChapterError(Manhwa manhwa, int current, int total) {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> manhwa.setChapters(current, total));
        assertEquals(CHAPTER_ERROR_MESSAGE, exception.getMessage());
    }

    private static void assertTagError(Manhwa manhwa, String tag) {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> manhwa.addTag(tag));
        assertEquals(
                "Tag must be a single word without ',' or '|'.", exception.getMessage());
    }
}
