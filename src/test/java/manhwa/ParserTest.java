package manhwa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import manhwa.commands.ByeCommand;
import manhwa.commands.Command;

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
                ManhwaTrackerException.class, () -> Parser.parseCommand("list"));

        assertEquals(
                "Unknown command. Type `help` to see available commands.",
                exception.getMessage());
    }
}
