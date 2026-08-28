package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StatusTest {
    @Test
    void fromString_validNames_returnsMatchingStatuses() throws ManhwaTrackerException {
        assertAll(
                () -> assertEquals(Status.WISHLIST, Status.fromString("wishlist")),
                () -> assertEquals(Status.ONGOING, Status.fromString("ongoing")),
                () -> assertEquals(Status.COMPLETED, Status.fromString("completed")));
    }

    @Test
    void fromString_mixedCaseName_returnsMatchingStatus() throws ManhwaTrackerException {
        assertEquals(Status.ONGOING, Status.fromString("OnGoInG"));
    }

    @Test
    void fromString_invalidName_throwsExceptionWithExactMessage() {
        ManhwaTrackerException exception = assertThrows(
                ManhwaTrackerException.class, () -> Status.fromString("paused"));

        assertEquals(
                "Unknown status: paused. Valid statuses: wishlist, ongoing, completed.",
                exception.getMessage());
    }
}
