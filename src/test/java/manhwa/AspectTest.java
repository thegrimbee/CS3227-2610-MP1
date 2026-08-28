package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AspectTest {
    @Test
    void fromString_validNames_returnsMatchingAspects() throws ManhwaTrackerException {
        assertAll(
                () -> assertEquals(Aspect.PLOT, Aspect.fromString("plot")),
                () -> assertEquals(Aspect.ART, Aspect.fromString("art")),
                () -> assertEquals(Aspect.UNIQUENESS, Aspect.fromString("uniqueness")),
                () -> assertEquals(Aspect.CHARACTERS, Aspect.fromString("characters")),
                () -> assertEquals(Aspect.PACING, Aspect.fromString("pacing")));
    }

    @Test
    void fromString_mixedCaseName_returnsMatchingAspect() throws ManhwaTrackerException {
        assertEquals(Aspect.UNIQUENESS, Aspect.fromString("UnIqUeNeSs"));
    }

    @Test
    void fromString_invalidName_throwsExceptionWithExactMessage() {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> Aspect.fromString("music"));

        assertEquals(
                "Unknown aspect: music. Valid aspects: plot, art, uniqueness, characters, pacing.",
                exception.getMessage());
    }
}
