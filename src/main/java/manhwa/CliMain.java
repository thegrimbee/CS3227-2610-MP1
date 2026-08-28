package manhwa;

import java.util.Scanner;

/**
 * Provides the minimal command-line entry point for ManhwaDex Lite.
 */
public final class CliMain {
    private static final String BYE_COMMAND = "bye";

    private CliMain() {
    }

    /**
     * Starts the command-line interface and processes input until {@code bye} or end-of-file.
     *
     * @param args command-line arguments, which are unused
     */
    public static void main(String[] args) {
        assert args != null;
        Scanner scanner = new Scanner(System.in);
        Ui ui = new CliUi(scanner, System.out);
        ui.showWelcome();

        while (scanner.hasNextLine()) {
            String command = ui.readLine().trim();
            if (command.isEmpty()) {
                continue;
            }
            if (command.equalsIgnoreCase(BYE_COMMAND)) {
                ui.showMessage(CliUi.GOODBYE_MESSAGE);
                return;
            }
            ui.showMessage(CliUi.UNKNOWN_COMMAND_MESSAGE);
        }
    }
}
