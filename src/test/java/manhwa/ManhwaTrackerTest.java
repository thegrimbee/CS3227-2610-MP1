package manhwa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ManhwaTrackerTest {
    @TempDir
    Path tempDirectory;

    @Test
    void getResponse_blankInput_returnsPromptMessage() {
        ManhwaTracker tracker = createTracker();

        assertEquals("Please enter a command.", tracker.getResponse("  \t"));
        assertFalse(tracker.isExit());
    }

    @Test
    void getResponse_unknownCommand_propagatesParserMessage() {
        ManhwaTracker tracker = createTracker();

        assertEquals(
                "Unknown command. Type `help` to see available commands.",
                tracker.getResponse("ajglskgj"));
        assertFalse(tracker.isExit());
    }

    @Test
    void getResponse_bye_returnsFarewellAndSetsExitFlag() {
        ManhwaTracker tracker = createTracker();

        assertEquals("Bye. See you next time!", tracker.getResponse("bye"));
        assertTrue(tracker.isExit());
    }

    private ManhwaTracker createTracker() {
        Storage storage = new Storage(tempDirectory.toString());
        return new ManhwaTracker(new ManhwaList(), null, storage);
    }
}
