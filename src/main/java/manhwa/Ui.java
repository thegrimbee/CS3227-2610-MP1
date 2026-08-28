package manhwa;

/**
 * Defines the input and output operations required by a ManhwaDex Lite user interface.
 */
public interface Ui {
    /**
     * Shows the application welcome message.
     */
    void showWelcome();

    /**
     * Shows a message to the user.
     *
     * @param message message to show
     */
    void showMessage(String message);

    /**
     * Reads the next input line.
     *
     * @return the next line of user input
     */
    String readLine();
}
