package manhwa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manhwa.commands.FindCommand;

class FindCommandTest {
    @TempDir
    Path tempDirectory;

    @Test
    void execute_matchingKeyword_returnsNumberedCaseInsensitiveMatches() throws Exception {
        Manhwa solo = new Manhwa("Solo Leveling", Status.ONGOING);
        solo.addTag("action");
        Manhwa max = new Manhwa("The Max-Level Player", Status.COMPLETED);
        Manhwa tower = new Manhwa("Tower of God", Status.WISHLIST);
        ManhwaList list = new ManhwaList();
        list.add(solo);
        list.add(tower);
        list.add(max);
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String response = new FindCommand("LEVEL").execute(list, null, storage, tracker);

        assertEquals(
                "Here are the matching entries in your list:" + System.lineSeparator()
                        + "1. [ONGOING] Solo Leveling  #action  -/-  Score: -"
                        + System.lineSeparator()
                        + "3. [COMPLETED] The Max-Level Player  -/-  Score: -",
                response);
    }

    @Test
    void execute_noMatchingKeyword_returnsExactNoMatchMessage() throws Exception {
        ManhwaList list = new ManhwaList();
        list.add(new Manhwa("Tower of God", Status.WISHLIST));
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaTracker tracker = new ManhwaTracker(list, null, storage);

        String response = new FindCommand("Solo Leveling")
                .execute(list, null, storage, tracker);

        assertEquals("No entries found matching 'Solo Leveling'.", response);
    }
}
