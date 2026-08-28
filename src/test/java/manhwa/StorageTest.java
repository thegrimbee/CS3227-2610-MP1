package manhwa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageTest {
    private static final String FILE_NAME = "manhwalist.txt";

    @TempDir
    Path tempDirectory;

    @Test
    void createDirAndFile_missingDirectory_createsDirectoryAndFile() throws IOException {
        Path dataDirectory = tempDirectory.resolve("nested").resolve("data");
        Storage storage = new Storage(dataDirectory.toString());

        storage.createDirAndFile();

        assertAll(
                () -> assertTrue(Files.isDirectory(dataDirectory)),
                () -> assertTrue(Files.isRegularFile(dataDirectory.resolve(FILE_NAME))));
    }

    @Test
    void saveAndLoad_populatedData_roundTrips() throws Exception {
        Storage storage = new Storage(tempDirectory.toString());
        PreferenceProfile profile = createProfile();
        Manhwa original = createManhwa();
        ManhwaList list = new ManhwaList();
        list.add(original);

        storage.saveData(list, profile);
        LoadResult result = storage.loadData();
        Manhwa restored = result.getManhwaList().get(1);

        assertAll(
                () -> assertEquals(1, result.getManhwaList().size()),
                () -> assertEquals(original.getTitle(), restored.getTitle()),
                () -> assertEquals(original.getStatus(), restored.getStatus()),
                () -> assertEquals(original.getTags(), restored.getTags()),
                () -> assertEquals(original.getRating(Aspect.PLOT),
                        restored.getRating(Aspect.PLOT)),
                () -> assertEquals(original.getCurrentChapter(), restored.getCurrentChapter()),
                () -> assertEquals(original.getTotalChapter(), restored.getTotalChapter()),
                () -> assertEquals(original.getNote(), restored.getNote()),
                () -> assertProfileEquals(profile, result.getPreferenceProfile()));
    }

    @Test
    void loadData_corruptLine_skipsLineAndPrintsWarning() throws Exception {
        Path file = tempDirectory.resolve(FILE_NAME);
        Files.writeString(file,
                "CORRUPT | not valid" + System.lineSeparator()
                        + "MANHWA | Valid | WISHLIST |  |  | 0/0 | ",
                StandardCharsets.UTF_8);
        Storage storage = new Storage(tempDirectory.toString());
        PrintStream originalError = System.err;
        ByteArrayOutputStream warningOutput = new ByteArrayOutputStream();

        LoadResult result;
        try {
            System.setErr(new PrintStream(warningOutput, true, StandardCharsets.UTF_8));
            result = storage.loadData();
        } finally {
            System.setErr(originalError);
        }

        assertAll(
                () -> assertEquals(1, result.getManhwaList().size()),
                () -> assertEquals("Valid", result.getManhwaList().get(1).getTitle()),
                () -> assertTrue(warningOutput.toString(StandardCharsets.UTF_8)
                        .contains("Warning: skipping corrupt line: CORRUPT | not valid")));
    }

    @Test
    void loadData_missingFile_returnsEmptyListAndNullProfile() throws IOException {
        Path absentDirectory = tempDirectory.resolve("absent");
        Storage storage = new Storage(absentDirectory.toString());

        LoadResult result = storage.loadData();

        assertAll(
                () -> assertEquals(0, result.getManhwaList().size()),
                () -> assertNull(result.getPreferenceProfile()),
                () -> assertFalse(Files.exists(absentDirectory.resolve(FILE_NAME))));
    }

    @Test
    void loadData_missingPreferenceLine_returnsNullProfile() throws Exception {
        Storage storage = new Storage(tempDirectory.toString());
        ManhwaList list = new ManhwaList();
        list.add(new Manhwa("Tower of God", Status.WISHLIST));

        storage.saveData(list, null);
        LoadResult result = storage.loadData();

        assertAll(
                () -> assertEquals(1, result.getManhwaList().size()),
                () -> assertNull(result.getPreferenceProfile()));
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

    private static Manhwa createManhwa() throws ManhwaTrackerException {
        Manhwa manhwa = new Manhwa("Solo Leveling", Status.ONGOING);
        manhwa.addTag("action");
        manhwa.setRating(Aspect.PLOT, 9);
        manhwa.setChapters(143, 179);
        manhwa.setNote("The art carries the story");
        return manhwa;
    }

    private static void assertProfileEquals(
            PreferenceProfile expected, PreferenceProfile actual) {
        for (Aspect aspect : Aspect.values()) {
            assertEquals(expected.getWeight(aspect), actual.getWeight(aspect));
        }
    }
}
