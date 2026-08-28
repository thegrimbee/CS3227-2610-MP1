package manhwa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import manhwa.commands.ByeCommand;
import manhwa.commands.Command;
import manhwa.commands.DeleteCommand;
import manhwa.commands.FindCommand;
import manhwa.commands.ListCommand;

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

    private static void assertInvalidCommand(String input, String expectedMessage) {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> Parser.parseCommand(input));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
