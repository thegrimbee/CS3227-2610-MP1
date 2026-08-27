package manhwa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Provides the minimal command-line entry point for ManhwaDex Lite.
 */
public final class CliMain {
    private static final String BYE_COMMAND = "bye";
    private static final String WELCOME_MESSAGE =
            "Welcome to ManhwaDex Lite! Type `help` to see available commands.";
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Unknown command. Type `help` to see available commands.";
    private static final String GOODBYE_MESSAGE = "Bye. See you next time!";

    private CliMain() {
    }

    /**
     * Starts the command-line interface and processes input until {@code bye} or end-of-file.
     *
     * @param args command-line arguments, which are unused
     * @throws IOException if an error occurs while reading standard input
     */
    public static void main(String[] args) throws IOException {
        assert args != null;
        System.out.println(WELCOME_MESSAGE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String input;
        while ((input = reader.readLine()) != null) {
            String command = input.trim();
            if (command.isEmpty()) {
                continue;
            }
            if (command.equalsIgnoreCase(BYE_COMMAND)) {
                System.out.println(GOODBYE_MESSAGE);
                return;
            }
            System.out.println(UNKNOWN_COMMAND_MESSAGE);
        }
    }
}
