package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import manhwa.commands.ByeCommand;
import manhwa.commands.ChapterCommand;
import manhwa.commands.Command;
import manhwa.commands.DeleteCommand;
import manhwa.commands.FindCommand;
import manhwa.commands.FilterCommand;
import manhwa.commands.ListCommand;
import manhwa.commands.TagCommand;
import manhwa.commands.UntagCommand;

class ParserTest {
    @Test
    void parseCommand_bye_returnsExitingByeCommand() throws ManhwaTrackerException {
        Command command = Parser.parseCommand("bye");

        assertInstanceOf(ByeCommand.class, command);
        assertTrue(command.isExit());
    }

    @Test
    void parseCommand_unknownCommand_throwsExceptionWithExactMessage() {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> Parser.parseCommand("wat"));

        assertEquals(
                "Unknown command. Type `help` to see available commands.",
                exception.getMessage());
    }

    @Test
    void parseCommand_listFormats_returnListCommands() throws ManhwaTrackerException {
        assertInstanceOf(ListCommand.class, Parser.parseCommand("list"));
        assertInstanceOf(ListCommand.class, Parser.parseCommand("list OnGoInG"));
    }

    @Test
    void parseCommand_invalidListFormat_throwsExpectedFormatMessage() {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> Parser.parseCommand("list ongoing extra"));

        assertEquals(
                "Invalid list command. Expected format: list [status].",
                exception.getMessage());
    }

    @Test
    void parseCommand_unknownListStatus_propagatesStatusMessage() {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> Parser.parseCommand("list paused"));

        assertEquals(
                "Unknown status: paused. Valid statuses: wishlist, ongoing, completed.",
                exception.getMessage());
    }

    @Test
    void parseCommand_deleteFormat_returnsDeleteCommand() throws ManhwaTrackerException {
        assertInstanceOf(DeleteCommand.class, Parser.parseCommand("delete 2"));
    }

    @Test
    void parseCommand_invalidDeleteFormats_throwExpectedFormatMessage() {
        assertInvalidCommand("delete", "Invalid delete command. Expected format: delete <index>.");
        assertInvalidCommand(
                "delete first", "Invalid delete command. Expected format: delete <index>.");
        assertInvalidCommand(
                "delete 1 extra", "Invalid delete command. Expected format: delete <index>.");
    }

    @Test
    void parseCommand_findFormatWithSpaces_returnsFindCommand()
            throws ManhwaTrackerException {
        assertInstanceOf(FindCommand.class, Parser.parseCommand("find Solo Leveling"));
    }

    @Test
    void parseCommand_missingFindKeyword_throwsExpectedFormatMessage() {
        assertInvalidCommand("find", "Invalid find command. Expected format: find <keyword>.");
    }

    @Test
    void parseCommand_tagFormat_returnsTagCommand() throws ManhwaTrackerException {
        assertInstanceOf(TagCommand.class, Parser.parseCommand("tag 2 action"));
    }

    @Test
    void parseCommand_invalidTagFormats_throwExpectedFormatMessage() {
        String message = "Invalid tag command. Expected format: tag <index> <tag>.";
        assertInvalidCommand("tag", message);
        assertInvalidCommand("tag first action", message);
        assertInvalidCommand("tag 1 dark fantasy", message);
    }

    @Test
    void parseCommand_untagFormat_returnsUntagCommand() throws ManhwaTrackerException {
        assertInstanceOf(UntagCommand.class, Parser.parseCommand("untag 2 action"));
    }

    @Test
    void parseCommand_invalidUntagFormats_throwExpectedFormatMessage() {
        String message = "Invalid untag command. Expected format: untag <index> <tag>.";
        assertInvalidCommand("untag", message);
        assertInvalidCommand("untag first action", message);
        assertInvalidCommand("untag 1 dark fantasy", message);
    }

    @Test
    void parseCommand_filterFormat_returnsFilterCommand() throws ManhwaTrackerException {
        assertInstanceOf(FilterCommand.class, Parser.parseCommand("filter action"));
    }

    @Test
    void parseCommand_chapterFormats_returnChapterCommands() throws ManhwaTrackerException {
        assertAll(
                () -> assertInstanceOf(
                        ChapterCommand.class, Parser.parseCommand("chapter 2")),
                () -> assertInstanceOf(
                        ChapterCommand.class, Parser.parseCommand("chapter 2 143")),
                () -> assertInstanceOf(
                        ChapterCommand.class, Parser.parseCommand("chapter 2 143 /of 179")),
                () -> assertInstanceOf(
                        ChapterCommand.class, Parser.parseCommand("chapter 2 143/of179")));
    }

    @Test
    void parseCommand_invalidChapterFormats_throwExpectedFormatMessage() {
        String message = "Invalid chapter command. Expected format: "
                + "chapter <index> <n> /of <m>. E.g. chapter 1 5 /of 10";
        assertAll(
                () -> assertInvalidCommand("chapter", message),
                () -> assertInvalidCommand("chapter first", message),
                () -> assertInvalidCommand("chapter 1 next", message),
                () -> assertInvalidCommand("chapter 1 12 total 20", message),
                () -> assertInvalidCommand("chapter 1 12 /of", message),
                () -> assertInvalidCommand("chapter 1 12 /of 20 extra", message));
    }

    @Test
    void parseCommand_invalidFilterFormats_throwExpectedFormatMessage() {
        String message = "Invalid filter command. Expected format: filter <tag>.";
        assertInvalidCommand("filter", message);
        assertInvalidCommand("filter dark fantasy", message);
    }

    private static void assertInvalidCommand(String input, String expectedMessage) {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> Parser.parseCommand(input));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
