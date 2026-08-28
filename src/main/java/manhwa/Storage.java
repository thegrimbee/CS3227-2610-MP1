package manhwa;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes ManhwaDex Lite data in a local text file.
 */
public class Storage {
    private static final String FILE_NAME = "manhwalist.txt";
    private static final String PREFERENCE_RECORD_TYPE = "PREF";
    private static final String MANHWA_RECORD_TYPE = "MANHWA";
    private static final String WARNING_PREFIX = "Warning: skipping corrupt line: ";

    private final Path directoryPath;
    private final Path filePath;

    /**
     * Creates storage rooted in the specified directory.
     *
     * @param directoryPath directory containing the data file
     */
    public Storage(String directoryPath) {
        assert directoryPath != null;
        this.directoryPath = Path.of(directoryPath);
        this.filePath = this.directoryPath.resolve(FILE_NAME);
    }

    /**
     * Creates the storage directory and data file when either is missing.
     *
     * @throws IOException if the directory or file cannot be created
     */
    public void createDirAndFile() throws IOException {
        Files.createDirectories(directoryPath);
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
    }

    /**
     * Rewrites the data file with the current profile and entries.
     *
     * @param manhwaList entries to save
     * @param profile preference profile to save, or {@code null} when absent
     * @throws UncheckedIOException if the data cannot be written
     */
    public void saveData(ManhwaList manhwaList, PreferenceProfile profile) {
        assert manhwaList != null;
        try {
            createDirAndFile();
            Files.write(filePath, serializeData(manhwaList, profile), StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to save ManhwaDex Lite data.", exception);
        }
    }

    /**
     * Loads all valid records, skipping corrupt lines with a warning.
     *
     * @return the loaded list and optional preference profile
     * @throws IOException if an existing data file cannot be read
     */
    public LoadResult loadData() throws IOException {
        ManhwaList manhwaList = new ManhwaList();
        PreferenceProfile profile = null;
        if (Files.notExists(filePath)) {
            return new LoadResult(manhwaList, null);
        }

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            try {
                String recordType = getRecordType(line);
                if (PREFERENCE_RECORD_TYPE.equals(recordType)) {
                    if (profile != null) {
                        throw new ManhwaTrackerException("Duplicate preference record.");
                    }
                    profile = PreferenceProfile.fromFileString(line);
                } else if (MANHWA_RECORD_TYPE.equals(recordType)) {
                    manhwaList.add(Manhwa.fromFileString(line));
                } else {
                    throw new ManhwaTrackerException("Unknown storage record.");
                }
            } catch (ManhwaTrackerException exception) {
                System.err.println(WARNING_PREFIX + line);
            }
        }
        return new LoadResult(manhwaList, profile);
    }

    private List<String> serializeData(ManhwaList manhwaList, PreferenceProfile profile) {
        List<String> lines = new ArrayList<>();
        if (profile != null) {
            lines.add(profile.toFileString());
        }
        for (int index = 1; index <= manhwaList.size(); index++) {
            lines.add(getEntryForSave(manhwaList, index).toFileString());
        }
        return lines;
    }

    private Manhwa getEntryForSave(ManhwaList manhwaList, int index) {
        try {
            return manhwaList.get(index);
        } catch (ManhwaTrackerException exception) {
            throw new IllegalStateException("Manhwa list size changed while saving.", exception);
        }
    }

    private String getRecordType(String line) {
        int separatorIndex = line.indexOf('|');
        if (separatorIndex < 0) {
            return line.trim();
        }
        return line.substring(0, separatorIndex).trim();
    }
}
