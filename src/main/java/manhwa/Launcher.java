package manhwa;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Selects and starts the command-line or desktop interface for ManhwaDex Lite.
 */
public final class Launcher {
    private static final String CLI_ARGUMENT = "--cli";
    private static final String DATA_DIRECTORY = "data";
    private static final String WINDOW_TITLE = "ManhwaDex Lite";
    private static final String STARTUP_ERROR_MESSAGE =
            "Unable to start ManhwaDex Lite: ";

    private Launcher() {
    }

    /**
     * Starts the CLI when {@code --cli} is present, or the Swing interface otherwise.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        assert args != null;
        if (hasCliArgument(args)) {
            CliMain.main(args);
            return;
        }
        SwingUtilities.invokeLater(Launcher::launchGui);
    }

    private static boolean hasCliArgument(String[] args) {
        assert args != null;
        for (String argument : args) {
            if (CLI_ARGUMENT.equals(argument)) {
                return true;
            }
        }
        return false;
    }

    private static void launchGui() {
        try {
            Storage storage = new Storage(DATA_DIRECTORY);
            storage.createDirAndFile();
            LoadResult loadResult = storage.loadData();
            ManhwaTracker controller = new ManhwaTracker(
                    loadResult.getManhwaList(), loadResult.getPreferenceProfile(), storage);
            MainWindow window = new MainWindow(controller);
            window.setVisible(true);
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(
                    null,
                    STARTUP_ERROR_MESSAGE + exception.getMessage(),
                    WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
