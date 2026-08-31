package manhwa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Stores and provides views of the user's manhwa entries.
 */
public class ManhwaList {
    private static final int FIRST_INDEX = 1;
    private static final String DUPLICATE_TITLE_MESSAGE_PREFIX = "A title like '";
    private static final String DUPLICATE_TITLE_MESSAGE_SUFFIX = "' already exists.";

    private final List<Manhwa> entries;

    /**
     * Creates an empty manhwa list.
     */
    public ManhwaList() {
        entries = new ArrayList<>();
    }

    /**
     * Adds a manhwa when no case-insensitive title duplicate exists.
     *
     * @param manhwa entry to add
     * @throws ManhwaTrackerException if a similar title is already present
     */
    public void add(Manhwa manhwa) throws ManhwaTrackerException {
        assert manhwa != null;
        if (containsTitle(manhwa.getTitle())) {
            throw new ManhwaTrackerException(
                    DUPLICATE_TITLE_MESSAGE_PREFIX + manhwa.getTitle()
                            + DUPLICATE_TITLE_MESSAGE_SUFFIX);
        }
        entries.add(manhwa);
    }

    /**
     * Deletes the entry at a 1-based index.
     *
     * @param index 1-based entry index
     * @return the deleted entry
     * @throws ManhwaTrackerException if the index is outside the list
     */
    public Manhwa delete(int index) throws ManhwaTrackerException {
        validateIndex(index);
        return entries.remove(toInternalIndex(index));
    }

    /**
     * Returns the entry at a 1-based index.
     *
     * @param index 1-based entry index
     * @return the requested entry
     * @throws ManhwaTrackerException if the index is outside the list
     */
    public Manhwa get(int index) throws ManhwaTrackerException {
        validateIndex(index);
        return entries.get(toInternalIndex(index));
    }

    /**
     * Returns the number of entries.
     *
     * @return list size
     */
    public int size() {
        return entries.size();
    }

    /**
     * Returns the permanent 1-based index of an entry in this list.
     *
     * @param target entry whose index is requested
     * @return permanent index, or 0 when the entry is not present
     */
    public int getIndexOf(Manhwa target) {
        assert target != null;
        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index) == target) {
                return index + FIRST_INDEX;
            }
        }
        return 0;
    }

    /**
     * Replaces this list's contents with a loaded durable snapshot.
     *
     * @param source snapshot whose entries should become current
     */
    void replaceWith(ManhwaList source) {
        assert source != null;
        entries.clear();
        entries.addAll(source.entries);
    }

    /**
     * Finds entries whose titles contain a keyword without regard to letter case.
     *
     * @param keyword title keyword
     * @return a new list containing matching entries
     */
    public List<Manhwa> findByKeyword(String keyword) {
        assert keyword != null;
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        List<Manhwa> matches = new ArrayList<>();
        for (Manhwa manhwa : entries) {
            if (manhwa.getTitle().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                matches.add(manhwa);
            }
        }
        return matches;
    }

    /**
     * Selects entries with a particular reading status.
     *
     * @param status status to select
     * @return a new list containing matching entries
     */
    public List<Manhwa> filterByStatus(Status status) {
        assert status != null;
        List<Manhwa> matches = new ArrayList<>();
        for (Manhwa manhwa : entries) {
            if (manhwa.getStatus() == status) {
                matches.add(manhwa);
            }
        }
        return matches;
    }

    /**
     * Selects entries carrying a particular tag.
     *
     * @param tag tag to select
     * @return a new list containing matching entries
     */
    public List<Manhwa> filterByTag(String tag) {
        assert tag != null;
        List<Manhwa> matches = new ArrayList<>();
        for (Manhwa manhwa : entries) {
            if (manhwa.getTags().contains(tag)) {
                matches.add(manhwa);
            }
        }
        return matches;
    }

    /**
     * Returns a sorted copy without changing the stored order.
     *
     * @param sortKey ordering to apply
     * @param profile preference profile used for score sorting
     * @return a new sorted list
     */
    public List<Manhwa> sortedView(SortKey sortKey, PreferenceProfile profile) {
        assert sortKey != null;
        assert profile != null;
        List<Manhwa> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort(getComparator(sortKey, profile));
        return sortedEntries;
    }

    private boolean containsTitle(String title) {
        assert title != null;
        for (Manhwa manhwa : entries) {
            if (manhwa.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    private void validateIndex(int index) throws ManhwaTrackerException {
        if (index < FIRST_INDEX || index > entries.size()) {
            throw new ManhwaTrackerException("Entry " + index + " does not exist.");
        }
    }

    private int toInternalIndex(int index) {
        return index - FIRST_INDEX;
    }

    private Comparator<Manhwa> getComparator(SortKey sortKey, PreferenceProfile profile) {
        return switch (sortKey) {
        case SCORE -> Comparator.comparingDouble(
                (Manhwa manhwa) -> manhwa.getOverallScore(profile)).reversed();
        case TITLE -> Comparator.comparing(Manhwa::getTitle, String.CASE_INSENSITIVE_ORDER);
        case DATE -> Comparator.comparing(Manhwa::getDateAdded).reversed();
        case CHAPTERS -> Comparator.comparingInt(Manhwa::getCurrentChapter).reversed();
        case PLOT -> getRatingComparator(Aspect.PLOT);
        case ART -> getRatingComparator(Aspect.ART);
        case UNIQUENESS -> getRatingComparator(Aspect.UNIQUENESS);
        case CHARACTERS -> getRatingComparator(Aspect.CHARACTERS);
        case PACING -> getRatingComparator(Aspect.PACING);
        };
    }

    private Comparator<Manhwa> getRatingComparator(Aspect aspect) {
        assert aspect != null;
        return Comparator.comparing(
                (Manhwa manhwa) -> manhwa.getRating(aspect),
                Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
