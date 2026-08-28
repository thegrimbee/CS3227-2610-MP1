package manhwa.commands;

/**
 * Re-runs the same preference flow used for initial onboarding.
 */
public class RerankCommand extends OnboardCommand {
    public static final String COMMAND_WORD = "rerank";

    /**
     * Creates a preference-reranking command.
     */
    public RerankCommand() {
        super();
    }
}
