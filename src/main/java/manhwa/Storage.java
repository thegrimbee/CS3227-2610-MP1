package manhwa;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads and writes ManhwaDex Lite data in a local text file.
 */
public class Storage {
    private static final String FILE_NAME = "manhwalist.txt";
    private static final String BACKUP_FILE_NAME = FILE_NAME + ".bak";
    private static final String LOCK_FILE_NAME = FILE_NAME + ".lock";
    private static final String TEMP_FILE_PREFIX = FILE_NAME + ".";
    private static final String TEMP_FILE_SUFFIX = ".tmp";
    private static final String PREFERENCE_RECORD_TYPE = "PREF";
    private static final String MANHWA_RECORD_TYPE = "MANHWA";
    private static final String WARNING_PREFIX = "Warning: skipping corrupt line: ";
    private static final String SAVE_ERROR_MESSAGE = "Unable to save ManhwaDex Lite data.";
    private static final String CONFLICT_ERROR_MESSAGE =
            "The data file changed after it was loaded. Reload before saving again.";
    private static final Object JVM_FILE_LOCK = new Object();

    private final Path directoryPath;
    private final Path filePath;
    private final Path backupPath;
    private final Path lockPath;
    private byte[] lastKnownContent;

    /**
     * Creates storage rooted in the specified directory.
     *
     * @param directoryPath directory containing the data file
     */
    public Storage(String directoryPath) {
        assert directoryPath != null;
        this.directoryPath = Path.of(directoryPath);
        this.filePath = this.directoryPath.resolve(FILE_NAME);
        this.backupPath = this.directoryPath.resolve(BACKUP_FILE_NAME);
        this.lockPath = this.directoryPath.resolve(LOCK_FILE_NAME);
    }

    /**
     * Creates the storage directory and data file when either is missing.
     *
     * @throws IOException if the directory or file cannot be created
     */
    public void createDirAndFile() throws IOException {
        Files.createDirectories(directoryPath);
        if (Files.notExists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (FileAlreadyExistsException exception) {
                // Another application instance created the file after the existence check.
            }
        }
    }

    /**
     * Durably replaces the data file with the current profile and entries.
     *
     * @param manhwaList entries to save
     * @param profile preference profile to save, or {@code null} when absent
     * @throws UncheckedIOException if the data cannot be written or changed after loading
     */
    public void saveData(ManhwaList manhwaList, PreferenceProfile profile) {
        assert manhwaList != null;
        byte[] serializedData = serializeDataBytes(manhwaList, profile);
        try {
            createDirAndFile();
            synchronized (JVM_FILE_LOCK) {
                try (FileChannel lockChannel = openLockChannel();
                        FileLock fileLock = lockChannel.lock()) {
                    assert fileLock.isValid();
                    saveWhileLocked(serializedData);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(SAVE_ERROR_MESSAGE, exception);
        }
    }

    /**
     * Loads all valid records, skipping corrupt lines with a warning.
     *
     * @return the loaded list and optional preference profile
     * @throws IOException if an existing data file cannot be read
     */
    public LoadResult loadData() throws IOException {
        if (Files.notExists(filePath)) {
            lastKnownContent = new byte[0];
            return new LoadResult(new ManhwaList(), null);
        }

        byte[] storedData;
        synchronized (JVM_FILE_LOCK) {
            try (FileChannel lockChannel = openLockChannel();
                    FileLock fileLock = lockChannel.lock()) {
                assert fileLock.isValid();
                storedData = Files.readAllBytes(filePath);
                lastKnownContent = storedData.clone();
            }
        }
        return parseData(storedData);
    }

    private LoadResult parseData(byte[] storedData) {
        ManhwaList manhwaList = new ManhwaList();
        PreferenceProfile profile = null;
        for (String line : new String(storedData, StandardCharsets.UTF_8).lines().toList()) {
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

    private FileChannel openLockChannel() throws IOException {
        return FileChannel.open(lockPath,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    private void saveWhileLocked(byte[] serializedData) throws IOException {
        byte[] currentContent = Files.readAllBytes(filePath);
        ensureFileHasNotChanged(currentContent);

        Path temporaryFile = Files.createTempFile(
                directoryPath, TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        try {
            writeAndForce(temporaryFile, serializedData);
            saveBackup(currentContent);
            replaceDataFile(temporaryFile);
            lastKnownContent = serializedData.clone();
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private void ensureFileHasNotChanged(byte[] currentContent) throws IOException {
        if (lastKnownContent == null) {
            if (currentContent.length > 0) {
                throw new IOException(CONFLICT_ERROR_MESSAGE);
            }
            return;
        }
        if (!Arrays.equals(lastKnownContent, currentContent)) {
            throw new IOException(CONFLICT_ERROR_MESSAGE);
        }
    }

    private void saveBackup(byte[] currentContent) throws IOException {
        if (currentContent.length == 0) {
            return;
        }
        Path temporaryBackup = Files.createTempFile(
                directoryPath, BACKUP_FILE_NAME + ".", TEMP_FILE_SUFFIX);
        try {
            writeAndForce(temporaryBackup, currentContent);
            replaceFile(temporaryBackup, backupPath);
        } finally {
            Files.deleteIfExists(temporaryBackup);
        }
    }

    private void writeAndForce(Path path, byte[] content) throws IOException {
        Files.write(path, content,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    void replaceDataFile(Path temporaryFile) throws IOException {
        replaceFile(temporaryFile, filePath);
    }

    private void replaceFile(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private byte[] serializeDataBytes(
            ManhwaList manhwaList, PreferenceProfile profile) {
        List<String> lines = serializeData(manhwaList, profile);
        if (lines.isEmpty()) {
            return new byte[0];
        }
        String content = String.join(System.lineSeparator(), lines)
                + System.lineSeparator();
        return content.getBytes(StandardCharsets.UTF_8);
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
