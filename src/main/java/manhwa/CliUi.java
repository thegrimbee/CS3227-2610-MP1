package manhwa;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Provides console input and output for ManhwaDex Lite.
 */
public class CliUi implements Ui {
    static final String UNKNOWN_COMMAND_MESSAGE =
            "Unknown command. Type `help` to see available commands.";
    static final String GOODBYE_MESSAGE = "Bye. See you next time!";

    private static final String WELCOME_MESSAGE =
            "Welcome to ManhwaDex Lite! Type `help` to see available commands.";

    private final Scanner scanner;
    private final PrintStream out;

    /**
     * Creates a command-line user interface using the supplied streams.
     *
     * @param scanner scanner used to read input
     * @param out stream used to print output
     */
    public CliUi(Scanner scanner, PrintStream out) {
        assert scanner != null;
        assert out != null;
        this.scanner = scanner;
        this.out = out;
    }

    /**
     * Prints the application welcome message.
     */
    @Override
    public void showWelcome() {
        out.println(WELCOME_MESSAGE);
    }

    /**
     * Prints a message followed by a line separator.
     *
     * @param message message to print
     */
    @Override
    public void showMessage(String message) {
        assert message != null;
        out.println(message);
    }

    /**
     * Reads and returns the next console input line.
     *
     * @return the next input line
     */
    @Override
    public String readLine() {
        return scanner.nextLine();
    }
}
