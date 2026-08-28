package manhwa;

/**
 * Represents what input the controller is currently waiting for.
 */
public enum ConversationState {
    IDLE,
    AWAITING_STATUS,
    AWAITING_RATINGS,
    AWAITING_IMPORTANCE
}
