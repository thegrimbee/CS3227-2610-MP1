package manhwa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents one manhwa in the user's reading list.
 */
public class Manhwa {
    private static final String FILE_TYPE = "MANHWA";
    private static final String FIELD_SEPARATOR = " | ";
    private static final String TAG_SEPARATOR = ",";
    private static final String RATING_SEPARATOR = ";";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String CHAPTER_SEPARATOR = "/";
    private static final String NO_CHAPTER_DISPLAY = "-/-";
    private static final String CHAPTER_PREFIX = "ch. ";
    private static final String TAG_ERROR_MESSAGE =
            "Tag must be a single word without ',' or '|'.";
    private static final String NOTE_ERROR_MESSAGE = "Note cannot contain '|'.";
    private static final String RATING_ERROR_MESSAGE =
            "Rating must be an integer from 1 to 10.";
    private static final String CHAPTER_ERROR_MESSAGE =
            "Invalid chapter: current must be at least 1, total must be 0 or higher, "
                    + "and current cannot exceed total.";
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 10;
    private static final int MIN_CURRENT_CHAPTER = 1;
    private static final int UNKNOWN_TOTAL_CHAPTER = 0;
    private static final int NO_CHAPTER = 0;
    private static final int SCORE_DECIMAL_PLACES = 1;
    private static final double NO_OVERALL_SCORE = -1.0;
    private static final int MANHWA_FIELD_COUNT = 7;

    private final String title;
    private Status status;
    private final LocalDate dateAdded;
    private final Map<Aspect, Integer> ratings;
    private int currentChapter;
    private int totalChapter;
    private final List<String> tags;
    private String note;

    /**
     * Creates a manhwa with no ratings, progress, tags, or note.
     *
     * @param title title of the manhwa
     * @param status initial reading status
     */
    public Manhwa(String title, Status status) {
        assert title != null;
        assert status != null;
        this.title = title;
        this.status = status;
        this.dateAdded = LocalDate.now();
        this.ratings = new EnumMap<>(Aspect.class);
        this.tags = new ArrayList<>();
    }

    /**
     * Returns the title.
     *
     * @return the manhwa title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the reading status.
     *
     * @return the reading status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Changes the reading status.
     *
     * @param status new reading status
     */
    public void setStatus(Status status) {
        assert status != null;
        this.status = status;
    }

    /**
     * Returns the date on which this entry was created.
     *
     * @return the date added
     */
    public LocalDate getDateAdded() {
        return dateAdded;
    }

    /**
     * Adds a tag if it is not already present.
     *
     * @param tag single-word tag to add
     * @throws ManhwaTrackerException if the tag is empty or contains whitespace or a delimiter
     */
    public void addTag(String tag) throws ManhwaTrackerException {
        assert tag != null;
        if (!isValidTag(tag)) {
            throw new ManhwaTrackerException(TAG_ERROR_MESSAGE);
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Removes a tag if it is present.
     *
     * @param tag tag to remove
     */
    public void removeTag(String tag) {
        assert tag != null;
        tags.remove(tag);
    }

    /**
     * Returns an unmodifiable copy of the tags.
     *
     * @return tags in insertion order
     */
    public List<String> getTags() {
        return List.copyOf(tags);
    }

    /**
     * Sets or clears the note.
     *
     * @param note new note, or {@code null} to clear it
     * @throws ManhwaTrackerException if the note contains a pipe
     */
    public void setNote(String note) throws ManhwaTrackerException {
        if (note != null && note.contains("|")) {
            throw new ManhwaTrackerException(NOTE_ERROR_MESSAGE);
        }
        this.note = note;
    }

    /**
     * Returns the note.
     *
     * @return the note, or {@code null} when there is no note
     */
    public String getNote() {
        return note;
    }

    /**
     * Sets a rating for one aspect.
     *
     * @param aspect aspect to rate
     * @param rating rating from 1 to 10
     * @throws ManhwaTrackerException if the rating is outside the allowed range
     */
    public void setRating(Aspect aspect, int rating) throws ManhwaTrackerException {
        assert aspect != null;
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new ManhwaTrackerException(RATING_ERROR_MESSAGE);
        }
        ratings.put(aspect, rating);
    }

    /**
     * Returns the rating for an aspect.
     *
     * @param aspect aspect whose rating is requested
     * @return the rating, or {@code null} when the aspect is unrated
     */
    public Integer getRating(Aspect aspect) {
        assert aspect != null;
        return ratings.get(aspect);
    }

    /**
     * Reports whether at least one aspect has been rated.
     *
     * @return {@code true} when this manhwa has a rating
     */
    public boolean isRated() {
        return !ratings.isEmpty();
    }

    /**
     * Sets current and total chapter progress.
     *
     * @param current current chapter
     * @param total total chapters, or 0 when unknown
     * @throws ManhwaTrackerException if the chapter values are invalid
     */
    public void setChapters(int current, int total) throws ManhwaTrackerException {
        if (current < MIN_CURRENT_CHAPTER || total < UNKNOWN_TOTAL_CHAPTER
                || total > UNKNOWN_TOTAL_CHAPTER && current > total) {
            throw new ManhwaTrackerException(CHAPTER_ERROR_MESSAGE);
        }
        currentChapter = current;
        totalChapter = total;
    }

    /**
     * Returns the current chapter.
     *
     * @return current chapter, or 0 when none has been read
     */
    public int getCurrentChapter() {
        return currentChapter;
    }

    /**
     * Returns the total number of chapters.
     *
     * @return total chapters, or 0 when unknown
     */
    public int getTotalChapter() {
        return totalChapter;
    }

    /**
     * Formats the chapter progress for display.
     *
     * @return formatted chapter progress
     */
    public String getChapterDisplay() {
        if (totalChapter > UNKNOWN_TOTAL_CHAPTER) {
            return CHAPTER_PREFIX + currentChapter + CHAPTER_SEPARATOR + totalChapter;
        }
        if (currentChapter > NO_CHAPTER) {
            return CHAPTER_PREFIX + currentChapter;
        }
        return NO_CHAPTER_DISPLAY;
    }

    /**
     * Calculates the weighted score using rated aspects only.
     *
     * @param profile preference weights to apply
     * @return the score rounded to one decimal place, or -1 when unrated
     */
    public double getOverallScore(PreferenceProfile profile) {
        assert profile != null;
        if (!isRated()) {
            return NO_OVERALL_SCORE;
        }

        int weightedRatingTotal = 0;
        int weightTotal = 0;
        for (Map.Entry<Aspect, Integer> rating : ratings.entrySet()) {
            int weight = profile.getWeight(rating.getKey());
            weightedRatingTotal += weight * rating.getValue();
            weightTotal += weight;
        }
        return BigDecimal.valueOf(weightedRatingTotal)
                .divide(BigDecimal.valueOf(weightTotal), SCORE_DECIMAL_PLACES, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Serializes this entry as one pipe-delimited storage line.
     *
     * @return the storage line
     */
    public String toFileString() {
        return String.join(FIELD_SEPARATOR,
                FILE_TYPE,
                title,
                status.name(),
                serializeTags(),
                serializeRatings(),
                currentChapter + CHAPTER_SEPARATOR + totalChapter,
                note == null ? "" : note);
    }

    /**
     * Parses one pipe-delimited manhwa storage line.
     *
     * @param line storage line to parse
     * @return the parsed manhwa
     * @throws ManhwaTrackerException if the line is malformed
     */
    public static Manhwa fromFileString(String line) throws ManhwaTrackerException {
        assert line != null;
        String[] fields = line.split("\\|", -1);
        if (fields.length != MANHWA_FIELD_COUNT || !FILE_TYPE.equals(fields[0].trim())
                || fields[1].trim().isEmpty()) {
            throw malformedLine(line);
        }

        try {
            Manhwa manhwa = new Manhwa(fields[1].trim(), Status.fromString(fields[2].trim()));
            parseTags(fields[3].trim(), manhwa, line);
            parseRatings(fields[4].trim(), manhwa, line);
            parseChapters(fields[5].trim(), manhwa, line);
            manhwa.setNote(fields[6].trim().isEmpty() ? null : fields[6].trim());
            return manhwa;
        } catch (NumberFormatException exception) {
            throw malformedLine(line);
        }
    }

    private boolean isValidTag(String tag) {
        if (tag.isBlank() || tag.contains(TAG_SEPARATOR) || tag.contains("|")) {
            return false;
        }
        return tag.chars().noneMatch(Character::isWhitespace);
    }

    private String serializeTags() {
        return String.join(TAG_SEPARATOR, tags);
    }

    private String serializeRatings() {
        StringJoiner serializedRatings = new StringJoiner(RATING_SEPARATOR);
        for (Aspect aspect : Aspect.values()) {
            Integer rating = ratings.get(aspect);
            if (rating != null) {
                serializedRatings.add(aspect.getDisplayName() + KEY_VALUE_SEPARATOR + rating);
            }
        }
        return serializedRatings.toString();
    }

    private static void parseTags(String value, Manhwa manhwa, String line)
            throws ManhwaTrackerException {
        if (value.isEmpty()) {
            return;
        }
        for (String tag : value.split(TAG_SEPARATOR, -1)) {
            if (tag.isEmpty() || manhwa.tags.contains(tag)) {
                throw malformedLine(line);
            }
            manhwa.addTag(tag);
        }
    }

    private static void parseRatings(String value, Manhwa manhwa, String line)
            throws ManhwaTrackerException {
        if (value.isEmpty()) {
            return;
        }
        for (String serializedRating : value.split(RATING_SEPARATOR, -1)) {
            String[] ratingParts = serializedRating.split(KEY_VALUE_SEPARATOR, -1);
            if (ratingParts.length != 2) {
                throw malformedLine(line);
            }
            Aspect aspect = Aspect.fromString(ratingParts[0].trim());
            if (manhwa.ratings.containsKey(aspect)) {
                throw malformedLine(line);
            }
            manhwa.setRating(aspect, Integer.parseInt(ratingParts[1].trim()));
        }
    }

    private static void parseChapters(String value, Manhwa manhwa, String line)
            throws ManhwaTrackerException {
        String[] chapterParts = value.split(CHAPTER_SEPARATOR, -1);
        if (chapterParts.length != 2) {
            throw malformedLine(line);
        }
        int current = Integer.parseInt(chapterParts[0].trim());
        int total = Integer.parseInt(chapterParts[1].trim());
        if (current == NO_CHAPTER && total == UNKNOWN_TOTAL_CHAPTER) {
            return;
        }
        manhwa.setChapters(current, total);
    }

    private static ManhwaTrackerException malformedLine(String line) {
        return new ManhwaTrackerException("Malformed manhwa data: " + line);
    }
}
