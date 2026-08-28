package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PreferenceProfileTest {
    @Test
    void constructor_newProfile_hasValidWeightForEveryAspect() {
        PreferenceProfile profile = new PreferenceProfile();

        assertAll(
                () -> assertEquals(1, profile.getWeight(Aspect.PLOT)),
                () -> assertEquals(1, profile.getWeight(Aspect.ART)),
                () -> assertEquals(1, profile.getWeight(Aspect.UNIQUENESS)),
                () -> assertEquals(1, profile.getWeight(Aspect.CHARACTERS)),
                () -> assertEquals(1, profile.getWeight(Aspect.PACING)));
    }

    @Test
    void setWeight_validImportance_updatesWeight() throws ManhwaTrackerException {
        PreferenceProfile profile = new PreferenceProfile();

        profile.setWeight(Aspect.PLOT, 5);

        assertEquals(5, profile.getWeight(Aspect.PLOT));
    }

    @Test
    void setWeight_invalidImportance_throwsExceptionWithExactMessage() {
        PreferenceProfile profile = new PreferenceProfile();

        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> profile.setWeight(Aspect.PLOT, 0));

        assertEquals(
                "Importance must be an integer from 1 to 5.", exception.getMessage());
    }

    @Test
    void fileString_profile_roundTripsAllWeights() throws ManhwaTrackerException {
        PreferenceProfile original = createProfile();

        String serialized = original.toFileString();
        PreferenceProfile restored = PreferenceProfile.fromFileString(serialized);

        assertAll(
                () -> assertEquals(
                        "PREF | plot=5 | art=4 | uniqueness=5 | characters=3 | pacing=2",
                        serialized),
                () -> assertEquals(original.getWeight(Aspect.PLOT),
                        restored.getWeight(Aspect.PLOT)),
                () -> assertEquals(original.getWeight(Aspect.ART),
                        restored.getWeight(Aspect.ART)),
                () -> assertEquals(original.getWeight(Aspect.UNIQUENESS),
                        restored.getWeight(Aspect.UNIQUENESS)),
                () -> assertEquals(original.getWeight(Aspect.CHARACTERS),
                        restored.getWeight(Aspect.CHARACTERS)),
                () -> assertEquals(original.getWeight(Aspect.PACING),
                        restored.getWeight(Aspect.PACING)));
    }

    @Test
    void fromFileString_malformedLine_throwsTrackerException() {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class,
                () -> PreferenceProfile.fromFileString("PREF | plot=5"));

        assertTrue(exception.getMessage().startsWith("Malformed preference data:"));
    }

    private static PreferenceProfile createProfile() throws ManhwaTrackerException {
        PreferenceProfile profile = new PreferenceProfile();
        profile.setWeight(Aspect.PLOT, 5);
        profile.setWeight(Aspect.ART, 4);
        profile.setWeight(Aspect.UNIQUENESS, 5);
        profile.setWeight(Aspect.CHARACTERS, 3);
        profile.setWeight(Aspect.PACING, 2);
        return profile;
    }
}
