package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manhwa.commands.TagCommand;

class TagCommandTest {
    @TempDir
    Path tempDirectory;

    @Test
    void tag_addAndDuplicate_keepsSingleTagAndPersists() throws Exception {
        Manhwa manhwa = new Manhwa("Solo Leveling", Status.ONGOING);
        ManhwaList list = listWith(manhwa);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String firstResponse = tracker.getResponse("tag 1 action");
        String duplicateResponse = tracker.getResponse("tag 1 action");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals(
                        "Added tag #action to \"Solo Leveling\".", firstResponse),
                () -> assertEquals(
                        "Added tag #action to \"Solo Leveling\".", duplicateResponse),
                () -> assertEquals(List.of("action"), manhwa.getTags()),
                () -> assertEquals(List.of("action"), restored.getTags()));
    }

    @Test
    void tag_spaceInTag_surfacesDomainValidationMessage() throws Exception {
        ManhwaList list = listWith(new Manhwa("Tower of God", Status.ONGOING));
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class,
                () -> new TagCommand(1, "dark fantasy")
                        .execute(list, null, storage, tracker));

        assertEquals(
                "Tag must be a single word without ',' or '|'.", exception.getMessage());
    }

    @Test
    void tag_commaDelimiter_isRejectedWithoutChangingStoredEntry() throws Exception {
        Manhwa manhwa = new Manhwa("Tower of God", Status.ONGOING);
        ManhwaList list = listWith(manhwa);
        Storage storage = new Storage(tempDirectory.toString());
        storage.saveData(list, null);
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String response = tracker.getResponse("tag 1 action,fantasy");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals(
                        "Tag must be a single word without ',' or '|'.", response),
                () -> assertTrue(manhwa.getTags().isEmpty()),
                () -> assertTrue(restored.getTags().isEmpty()));
    }

    @Test
    void untag_existingAndMissingTag_returnsExpectedMessagesAndPersists()
            throws Exception {
        Manhwa manhwa = new Manhwa("Eleceed", Status.ONGOING);
        manhwa.addTag("action");
        ManhwaList list = listWith(manhwa);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String removedResponse = tracker.getResponse("untag 1 action");
        String missingResponse = tracker.getResponse("untag 1 action");
        Manhwa restored = storage.loadData().getManhwaList().get(1);

        assertAll(
                () -> assertEquals(
                        "Removed tag #action from \"Eleceed\".", removedResponse),
                () -> assertEquals(
                        "\"Eleceed\" has no tag #action.", missingResponse),
                () -> assertTrue(manhwa.getTags().isEmpty()),
                () -> assertTrue(restored.getTags().isEmpty()));
    }

    @Test
    void filter_hitAndMiss_returnsNumberedMatchesAndExactMissMessage()
            throws Exception {
        Manhwa solo = new Manhwa("Solo Leveling", Status.COMPLETED);
        solo.addTag("action");
        Manhwa romance = new Manhwa("Romance 101", Status.COMPLETED);
        romance.addTag("romance");
        Manhwa mixed = new Manhwa("Doom Breaker", Status.ONGOING);
        mixed.addTag("action");
        ManhwaTracker tracker = new ManhwaTracker(
                listWith(solo, romance, mixed), null,
                new Storage(tempDirectory.toString()));

        String matches = tracker.getResponse("filter action");
        String noMatches = tracker.getResponse("filter murim");

        assertAll(
                () -> assertTrue(matches.startsWith(
                        "Here are the entries with tag '#action':")),
                () -> assertTrue(matches.contains("1. [COMPLETED] Solo Leveling")),
                () -> assertTrue(matches.contains("2. [ONGOING] Doom Breaker")),
                () -> assertEquals(
                        "No entries found with tag '#murim'.", noMatches));
    }

    private static ManhwaList listWith(Manhwa... entries) throws ManhwaTrackerException {
        ManhwaList list = new ManhwaList();
        for (Manhwa entry : entries) {
            list.add(entry);
        }
        return list;
    }
}
