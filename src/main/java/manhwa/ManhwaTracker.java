package manhwa;

import java.io.IOException;
import java.io.UncheckedIOException;
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
    private static final String IMPORTANCE_ERROR_PREFIX =
            "Importance must be an integer from 1 to 5. ";
    private static final String ONBOARDING_GREETING =
            "Let's set up your scoring preferences! "
                    + "Rate how important each aspect is from 1 to 5.";
    private static final String PREFERENCES_SAVED_MESSAGE =
            "Preferences saved! Overall scores will now use your priorities. "
                    + "You can change your scoring mechanism by rerunning the onboard command.";
    private static final String SAVE_FAILED_MESSAGE =
            "Unable to save ManhwaDex Lite data. No changes were kept; "
                    + "the latest saved data was reloaded.";
    private static final String SAVE_RECOVERY_FAILED_MESSAGE =
            "Unable to save ManhwaDex Lite data, and the saved data could not be reloaded. "
                    + "Restart the application before making more changes.";
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 10;
    private static final int MIN_IMPORTANCE = 1;
    private static final int MAX_IMPORTANCE = 5;

    private final ManhwaList manhwaList;
    private PreferenceProfile profile;
    private final Storage storage;
    private ConversationState state = ConversationState.IDLE;
    private Manhwa pendingManhwa;
    private Manhwa statusTargetManhwa;
    private Status pendingStatus;
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
        } catch (UncheckedIOException exception) {
            return recoverFromSaveFailure();
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
        statusTargetManhwa = null;
        pendingStatus = null;
        nextAspectIndex = 0;
        state = ConversationState.AWAITING_STATUS;
    }

    /**
     * Starts collecting missing ratings before applying a status change.
     *
     * @param manhwa stored entry whose status will change
     * @param status target status
     * @return prompt for the first unrated aspect
     * @throws ManhwaTrackerException if an existing rating cannot be copied
     */
    public String startStatusFlow(Manhwa manhwa, Status status)
            throws ManhwaTrackerException {
        assert manhwa != null;
        assert status != null;
        statusTargetManhwa = manhwa;
        pendingManhwa = copyRatings(manhwa);
        pendingStatus = status;
        nextAspectIndex = findNextUnratedAspectIndex(0);
        assert nextAspectIndex < Aspect.values().length;
        state = ConversationState.AWAITING_RATINGS;
        return getRatingPrompt(Aspect.values()[nextAspectIndex]);
    }

    /**
     * Starts collecting a new preference profile.
     *
     * @return prompt for the first aspect's importance
     */
    public String startOnboardingFlow() {
        pendingProfile = new PreferenceProfile();
        nextAspectIndex = 0;
        state = ConversationState.AWAITING_IMPORTANCE;
        return ONBOARDING_GREETING + System.lineSeparator()
                + getImportancePrompt(Aspect.values()[nextAspectIndex]);
    }

    /**
     * Starts onboarding when no preference profile was loaded.
     *
     * @return the first onboarding prompt, or {@code null} when a profile already exists
     */
    public String startOnboardingIfNeeded() {
        if (profile != null || state != ConversationState.IDLE) {
            return null;
        }
        return startOnboardingFlow();
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
        case AWAITING_IMPORTANCE -> handleImportanceInput(input);
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

    private String handleImportanceInput(String input) throws ManhwaTrackerException {
        assert pendingProfile != null;
        Aspect aspect = Aspect.values()[nextAspectIndex];
        Integer importance = parseImportance(input);
        if (importance == null) {
            return IMPORTANCE_ERROR_PREFIX + getImportancePrompt(aspect);
        }

        pendingProfile.setWeight(aspect, importance);
        nextAspectIndex++;
        if (nextAspectIndex == Aspect.values().length) {
            return finishOnboarding();
        }
        return getImportancePrompt(Aspect.values()[nextAspectIndex]);
    }

    private String handleRatingInput(String input) throws ManhwaTrackerException {
        assert pendingManhwa != null;
        Aspect aspect = Aspect.values()[nextAspectIndex];
        Integer rating = parseRating(input);
        if (rating == null) {
            return RATING_ERROR_PREFIX + getRatingPrompt(aspect);
        }

        pendingManhwa.setRating(aspect, rating);
        nextAspectIndex = findNextUnratedAspectIndex(nextAspectIndex + 1);
        if (nextAspectIndex == Aspect.values().length) {
            return completeRatingsFlow();
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

    private Integer parseImportance(String input) {
        try {
            int importance = Integer.parseInt(input.trim());
            if (importance >= MIN_IMPORTANCE && importance <= MAX_IMPORTANCE) {
                return importance;
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

    private String getImportancePrompt(Aspect aspect) {
        assert aspect != null;
        return "Importance of " + aspect.getDisplayName() + " (1-5):";
    }

    private int findNextUnratedAspectIndex(int startIndex) {
        assert pendingManhwa != null;
        Aspect[] aspects = Aspect.values();
        for (int index = startIndex; index < aspects.length; index++) {
            if (pendingManhwa.getRating(aspects[index]) == null) {
                return index;
            }
        }
        return aspects.length;
    }

    private Manhwa copyRatings(Manhwa source) throws ManhwaTrackerException {
        assert source != null;
        Manhwa copy = new Manhwa(source.getTitle(), source.getStatus());
        for (Aspect aspect : Aspect.values()) {
            Integer rating = source.getRating(aspect);
            if (rating != null) {
                copy.setRating(aspect, rating);
            }
        }
        return copy;
    }

    private String completeRatingsFlow() throws ManhwaTrackerException {
        if (pendingStatus != null) {
            return finishStatusChange();
        }
        return finishAdd();
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

    private String finishOnboarding() {
        assert pendingProfile != null;
        profile = pendingProfile;
        storage.saveData(manhwaList, profile);
        resetConversation();
        return PREFERENCES_SAVED_MESSAGE;
    }

    private String finishStatusChange() throws ManhwaTrackerException {
        assert pendingManhwa != null;
        assert statusTargetManhwa != null;
        assert pendingStatus != null;
        Manhwa targetManhwa = statusTargetManhwa;
        Status targetStatus = pendingStatus;
        for (Aspect aspect : Aspect.values()) {
            Integer rating = pendingManhwa.getRating(aspect);
            assert rating != null;
            targetManhwa.setRating(aspect, rating);
        }
        targetManhwa.setStatus(targetStatus);
        storage.saveData(manhwaList, profile);
        resetConversation();
        return "Moved \"" + targetManhwa.getTitle() + "\" to "
                + targetStatus.getDisplayName() + ".";
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

    private String recoverFromSaveFailure() {
        resetConversation();
        try {
            LoadResult loadResult = storage.loadData();
            manhwaList.replaceWith(loadResult.getManhwaList());
            profile = loadResult.getPreferenceProfile();
            return SAVE_FAILED_MESSAGE;
        } catch (IOException exception) {
            return SAVE_RECOVERY_FAILED_MESSAGE;
        }
    }

    private void resetConversation() {
        state = ConversationState.IDLE;
        pendingManhwa = null;
        statusTargetManhwa = null;
        pendingStatus = null;
        pendingProfile = null;
        nextAspectIndex = 0;
    }
}
