package manhwa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

class CliUiTest {
    private static final String WELCOME_MESSAGE =
            "Welcome to ManhwaDex Lite! Type `help` to see available commands.";

    @Test
    void showWelcome_printsWelcomeText() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CliUi ui = createUi("", output);

        ui.showWelcome();

        assertEquals(WELCOME_MESSAGE + System.lineSeparator(), output.toString(
                StandardCharsets.UTF_8));
    }

    @Test
    void showMessage_printsSuppliedMessage() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CliUi ui = createUi("", output);

        ui.showMessage("Reading list updated.");

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Reading list updated."));
    }

    @Test
    void readLine_returnsInjectedInputLine() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CliUi ui = createUi("add Solo Leveling", output);

        assertEquals("add Solo Leveling", ui.readLine());
    }

    private static CliUi createUi(String input, ByteArrayOutputStream output) {
        Scanner scanner = new Scanner(new StringReader(input));
        PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8);
        return new CliUi(scanner, printStream);
    }
}
