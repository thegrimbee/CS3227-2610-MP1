package manhwa;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Provides the Swing conversation window for ManhwaDex Lite.
 */
public final class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "ManhwaDex Lite";
    private static final String WELCOME_MESSAGE =
            "Welcome to ManhwaDex Lite! Type `help` to see available commands.";
    private static final String USER_INPUT_PREFIX = "> ";
    private static final String FONT_NAME = Font.SANS_SERIF;
    private static final int FONT_SIZE = 14;
    private static final int LOG_ROWS = 24;
    private static final int LOG_COLUMNS = 72;
    private static final int OUTER_PADDING = 12;
    private static final int COMPONENT_GAP = 8;
    private static final int TEXT_MARGIN = 8;

    private final transient ManhwaTracker controller;
    private final JTextArea conversationLog;
    private final JTextField inputField;

    /**
     * Creates a window backed by the supplied application controller.
     *
     * @param controller controller shared with the command-line interface
     */
    public MainWindow(ManhwaTracker controller) {
        super(WINDOW_TITLE);
        assert controller != null;
        this.controller = controller;
        conversationLog = createConversationLog();
        inputField = createInputField();
        configureWindow();
        showStartupMessages();
    }

    private JTextArea createConversationLog() {
        JTextArea log = new JTextArea(LOG_ROWS, LOG_COLUMNS);
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        log.setMargin(createTextInsets());
        return log;
    }

    private JTextField createInputField() {
        JTextField field = new JTextField();
        field.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        field.setMargin(createTextInsets());
        field.addActionListener(event -> submitInput());
        return field;
    }

    private Insets createTextInsets() {
        return new Insets(TEXT_MARGIN, TEXT_MARGIN, TEXT_MARGIN, TEXT_MARGIN);
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(COMPONENT_GAP, COMPONENT_GAP));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(
                OUTER_PADDING, OUTER_PADDING, OUTER_PADDING, OUTER_PADDING));
        add(new JScrollPane(conversationLog), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void showStartupMessages() {
        appendMessage(WELCOME_MESSAGE);
        String startupPrompt = controller.startOnboardingIfNeeded();
        if (startupPrompt != null) {
            appendMessage(startupPrompt);
        }
    }

    private void submitInput() {
        String input = inputField.getText();
        appendMessage(USER_INPUT_PREFIX + input);
        String response = controller.getResponse(input);
        appendMessage(response);
        inputField.setText("");
        if (controller.isExit()) {
            inputField.setEnabled(false);
        }
    }

    private void appendMessage(String message) {
        assert message != null;
        if (!conversationLog.getText().isEmpty()) {
            conversationLog.append(System.lineSeparator());
        }
        conversationLog.append(message);
        conversationLog.setCaretPosition(conversationLog.getDocument().getLength());
    }
}
