package manhwa.commands;

import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.PreferenceProfile;
import manhwa.Storage;

/**
 * Starts the interactive preference-onboarding flow.
 */
public class OnboardCommand extends Command {
    public static final String COMMAND_WORD = "onboard";

    /**
     * Creates an onboarding command.
     */
    public OnboardCommand() {
    }

    /**
     * Starts collecting importance weights without changing stored data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return prompt for the first aspect's importance
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) {
        assert list != null;
        assert storage != null;
        assert controller != null;
        return controller.startOnboardingFlow();
    }
}
