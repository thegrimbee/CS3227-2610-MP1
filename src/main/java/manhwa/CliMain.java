package manhwa;

import java.io.IOException;
import java.util.Scanner;

/**
 * Provides the minimal command-line entry point for ManhwaDex Lite.
 */
public final class CliMain {
    private static final String DATA_DIRECTORY = "data";
    private static final String STARTUP_ERROR_MESSAGE =
            "Unable to start ManhwaDex Lite: ";

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

        try {
            ManhwaTracker tracker = createTracker();
            ui.showWelcome();
            showStartupPrompt(ui, tracker);
            runCommandLoop(scanner, ui, tracker);
        } catch (IOException exception) {
            ui.showMessage(STARTUP_ERROR_MESSAGE + exception.getMessage());
        }
    }

    private static ManhwaTracker createTracker() throws IOException {
        Storage storage = new Storage(DATA_DIRECTORY);
        storage.createDirAndFile();
        LoadResult loadResult = storage.loadData();
        return new ManhwaTracker(
                loadResult.getManhwaList(), loadResult.getPreferenceProfile(), storage);
    }

    private static void showStartupPrompt(Ui ui, ManhwaTracker tracker) {
        assert ui != null;
        assert tracker != null;
        String startupPrompt = tracker.startOnboardingIfNeeded();
        if (startupPrompt != null) {
            ui.showMessage(startupPrompt);
        }
    }

    private static void runCommandLoop(Scanner scanner, Ui ui, ManhwaTracker tracker) {
        assert scanner != null;
        assert ui != null;
        assert tracker != null;
        while (!tracker.isExit() && scanner.hasNextLine()) {
            String response = tracker.getResponse(ui.readLine());
            ui.showMessage(response);
        }
    }
}
