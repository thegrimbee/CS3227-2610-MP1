package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manhwa.commands.AddCommand;
import manhwa.commands.ByeCommand;
import manhwa.commands.CancelCommand;
import manhwa.commands.ChapterCommand;
import manhwa.commands.DeleteCommand;
import manhwa.commands.FilterCommand;
import manhwa.commands.FindCommand;
import manhwa.commands.HelpCommand;
import manhwa.commands.ListCommand;
import manhwa.commands.NoteCommand;
import manhwa.commands.OnboardCommand;
import manhwa.commands.RateCommand;
import manhwa.commands.RerankCommand;
import manhwa.commands.SortCommand;
import manhwa.commands.StatusCommand;
import manhwa.commands.TagCommand;
import manhwa.commands.UntagCommand;

class HelpCommandTest {
    @TempDir
    Path tempDirectory;

    @Test
    void help_listsEveryImplementedCommandWithFormatHint() throws Exception {
        ManhwaTracker tracker = new ManhwaTracker(
                new ManhwaList(), null, new Storage(tempDirectory.toString()));

        List<String> outputLines = List.of(tracker.getResponse("help").split("\\R"));
        List<String> expectedFormats = List.of(
                OnboardCommand.COMMAND_WORD,
                AddCommand.COMMAND_WORD + " <title>",
                ListCommand.COMMAND_WORD + " [status]",
                FindCommand.COMMAND_WORD + " <keyword>",
                FilterCommand.COMMAND_WORD + " <tag>",
                SortCommand.COMMAND_WORD + " <score|title|date|chapters|plot|art|"
                        + "uniqueness|characters|pacing>",
                DeleteCommand.COMMAND_WORD + " <index>",
                StatusCommand.COMMAND_WORD + " <index> <wishlist|ongoing|completed>",
                RateCommand.COMMAND_WORD + " <index> <aspect> <1-10>",
                ChapterCommand.COMMAND_WORD + " <index> <n> /of <m>",
                TagCommand.COMMAND_WORD + " <index> <tag>",
                UntagCommand.COMMAND_WORD + " <index> <tag>",
                NoteCommand.COMMAND_WORD + " <index> [<text>|clear]",
                RerankCommand.COMMAND_WORD,
                HelpCommand.COMMAND_WORD,
                ByeCommand.COMMAND_WORD,
                CancelCommand.COMMAND_WORD);

        assertAll(
                () -> assertEquals(expectedFormats.size(), outputLines.size()),
                () -> assertTrue(outputLines.containsAll(expectedFormats)));
    }

    @Test
    void parser_helpWithoutArguments_returnsHelpCommand() throws ManhwaTrackerException {
        assertInstanceOf(HelpCommand.class, Parser.parseCommand("help"));
    }
}
