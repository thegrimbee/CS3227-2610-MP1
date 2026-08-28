package manhwa;

import java.util.Locale;

import manhwa.commands.Command;

/**
 * Coordinates command parsing and execution for ManhwaDex Lite.
 */
public class ManhwaTracker {
    private static final String BLANK_INPUT_MESSAGE = "Please enter a command.";
    private static final String CANCEL_WORD = "cancel";
    private static final String CANCELLED_MESSAGE = "Cancelled. Nothing was saved.";
    private static final String INVALID_STATUS_MESSAGE =
            "Please answer wishlist, ongoing, or completed.";
    private static final String RATING_ERROR_PREFIX =
            "Rating must be an integer from 1 to 10. ";
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 10;

    private final ManhwaList manhwaList;
    private PreferenceProfile profile;
    private final Storage storage;
    private ConversationState state = ConversationState.IDLE;
    private Manhwa pendingManhwa;
    private PreferenceProfile pendingProfile;
    private int nextAspectIndex;
    private boolean isExit;

    /**
     * Creates a controller using the loaded application data.
     *
     * @param manhwaList loaded manhwa entries
     * @param profile loaded preference profile, or {@code null} when absent
     * @param storage persistent storage
     */
    public ManhwaTracker(
            ManhwaList manhwaList, PreferenceProfile profile, Storage storage) {
        assert manhwaList != null;
        assert storage != null;
        this.manhwaList = manhwaList;
        this.profile = profile;
        this.storage = storage;
    }

    /**
     * Parses and executes one line of user input.
     *
     * @param input user input
     * @return the command response or user-facing validation message
     */
    public String getResponse(String input) {
        assert input != null;
        isExit = false;

        try {
            if (state != ConversationState.IDLE) {
                return handleConversationInput(input);
            }
            if (input.isBlank()) {
                return BLANK_INPUT_MESSAGE;
            }
            Command command = Parser.parseCommand(input);
            String response = command.execute(manhwaList, profile, storage, this);
            isExit = command.isExit();
            return response;
        } catch (ManhwaTrackerException exception) {
            return exception.getMessage();
        }
    }

    /**
     * Starts an add conversation for an entry that has not yet been saved.
     *
     * @param manhwa pending entry
     */
    public void startAddFlow(Manhwa manhwa) {
        assert manhwa != null;
        pendingManhwa = manhwa;
        nextAspectIndex = 0;
        state = ConversationState.AWAITING_STATUS;
    }

    /**
     * Returns the current conversation state.
     *
     * @return current state
     */
    public ConversationState getState() {
        return state;
    }

    /**
     * Reports whether the last successfully executed command requested an exit.
     *
     * @return {@code true} when the last command was an exit command
     */
    public boolean isExit() {
        return isExit;
    }

    private String handleConversationInput(String input) throws ManhwaTrackerException {
        if (input.trim().equalsIgnoreCase(CANCEL_WORD)) {
            resetConversation();
            return CANCELLED_MESSAGE;
        }
        return switch (state) {
        case AWAITING_STATUS -> handleStatusInput(input);
        case AWAITING_RATINGS -> handleRatingInput(input);
        case AWAITING_IMPORTANCE -> throw new IllegalStateException(
                "Importance conversation has not been started.");
        case IDLE -> throw new IllegalStateException("No conversation is in progress.");
        };
    }

    private String handleStatusInput(String input) throws ManhwaTrackerException {
        assert pendingManhwa != null;
        Status status;
        try {
            status = Status.fromString(input.trim());
        } catch (ManhwaTrackerException exception) {
            return INVALID_STATUS_MESSAGE;
        }

        pendingManhwa.setStatus(status);
        if (status == Status.WISHLIST) {
            return finishAdd();
        }
        nextAspectIndex = 0;
        state = ConversationState.AWAITING_RATINGS;
        return getRatingPrompt(Aspect.values()[nextAspectIndex]);
    }

    private String handleRatingInput(String input) throws ManhwaTrackerException {
        assert pendingManhwa != null;
        Aspect aspect = Aspect.values()[nextAspectIndex];
        Integer rating = parseRating(input);
        if (rating == null) {
            return RATING_ERROR_PREFIX + getRatingPrompt(aspect);
        }

        pendingManhwa.setRating(aspect, rating);
        nextAspectIndex++;
        if (nextAspectIndex == Aspect.values().length) {
            return finishAdd();
        }
        return getRatingPrompt(Aspect.values()[nextAspectIndex]);
    }

    private Integer parseRating(String input) {
        try {
            int rating = Integer.parseInt(input.trim());
            if (rating >= MIN_RATING && rating <= MAX_RATING) {
                return rating;
            }
        } catch (NumberFormatException exception) {
            return null;
        }
        return null;
    }

    private String getRatingPrompt(Aspect aspect) {
        assert aspect != null;
        assert pendingManhwa != null;
        return "Rate " + aspect.getDisplayName() + " (1-10) for \""
                + pendingManhwa.getTitle() + "\":";
    }

    private String finishAdd() throws ManhwaTrackerException {
        assert pendingManhwa != null;
        Manhwa completedManhwa = pendingManhwa;
        try {
            manhwaList.add(completedManhwa);
        } catch (ManhwaTrackerException exception) {
            resetConversation();
            throw exception;
        }
        storage.saveData(manhwaList, profile);
        resetConversation();
        return getAddConfirmation(completedManhwa);
    }

    private String getAddConfirmation(Manhwa manhwa) {
        assert manhwa != null;
        String prefix = "Got it. I've added \"" + manhwa.getTitle() + "\" to your ";
        if (manhwa.getStatus() == Status.WISHLIST) {
            return prefix + "wishlist.";
        }
        PreferenceProfile scoreProfile = profile == null ? new PreferenceProfile() : profile;
        String score = String.format(
                Locale.ROOT, "%.1f", manhwa.getOverallScore(scoreProfile));
        return prefix + manhwa.getStatus().getDisplayName()
                + " list (Score: " + score + ").";
    }

    private void resetConversation() {
        state = ConversationState.IDLE;
        pendingManhwa = null;
        pendingProfile = null;
        nextAspectIndex = 0;
    }
}
