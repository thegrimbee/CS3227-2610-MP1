package manhwa.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import manhwa.Aspect;
import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.ManhwaTracker;
import manhwa.ManhwaTrackerException;
import manhwa.PreferenceProfile;
import manhwa.SortKey;
import manhwa.Status;
import manhwa.Storage;

/**
 * Summarizes the user's reading list, ratings, tags, and preferences.
 */
public class StatsCommand extends Command {
    public static final String COMMAND_WORD = "stats";

    private static final String TOTAL_ENTRIES_PREFIX = "Total entries: ";
    private static final String AVERAGE_SCORE_PREFIX = "Average overall score (rated): ";
    private static final String NO_RATED_ENTRIES_MESSAGE = "No rated entries yet.";
    private static final String AVERAGE_RATINGS_PREFIX = "Average ratings:";
    private static final String TOTAL_CHAPTERS_PREFIX = "Total chapters read: ";
    private static final String TOP_SCORES_PREFIX = "Top 3 by score:";
    private static final String TOP_TAGS_PREFIX = "Top 3 tags:";
    private static final String TOP_PRIORITIES_PREFIX = "Top priority aspects:";
    private static final String NO_PROFILE_MESSAGE =
            "Run `onboard` first to set your priorities.";
    private static final String ITEM_SEPARATOR = " | ";
    private static final String ONE_DECIMAL_FORMAT = "%.1f";
    private static final int FIRST_ENTRY_INDEX = 1;
    private static final int COUNT_INCREMENT = 1;
    private static final int TOP_LIMIT = 3;

    /**
     * Calculates and formats all reading-list aggregates without changing stored data.
     *
     * @param list manhwa entries
     * @param profile preference profile, which may be {@code null}
     * @param storage persistent storage
     * @param controller application controller
     * @return eight aggregate lines in the specified order
     * @throws ManhwaTrackerException if stored entry access fails
     */
    @Override
    public String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException {
        assert list != null;
        assert storage != null;
        assert controller != null;
        List<Manhwa> entries = getEntries(list);
        PreferenceProfile scoringProfile = getScoringProfile(profile);
        return String.join(System.lineSeparator(),
                TOTAL_ENTRIES_PREFIX + list.size(),
                getStatusCounts(list),
                getAverageScore(entries, scoringProfile),
                getAverageRatings(entries),
                TOTAL_CHAPTERS_PREFIX + getTotalChapters(entries),
                getTopScores(list, scoringProfile),
                getTopTags(entries),
                getTopPriorities(profile));
    }

    private List<Manhwa> getEntries(ManhwaList list) throws ManhwaTrackerException {
        List<Manhwa> entries = new ArrayList<>();
        for (int index = FIRST_ENTRY_INDEX; index <= list.size(); index++) {
            entries.add(list.get(index));
        }
        return entries;
    }

    private PreferenceProfile getScoringProfile(PreferenceProfile profile) {
        if (profile == null) {
            return new PreferenceProfile();
        }
        return profile;
    }

    private String getStatusCounts(ManhwaList list) {
        int wishlist = list.filterByStatus(Status.WISHLIST).size();
        int ongoing = list.filterByStatus(Status.ONGOING).size();
        int completed = list.filterByStatus(Status.COMPLETED).size();
        return "Wishlist: " + wishlist + " | Ongoing: " + ongoing
                + " | Completed: " + completed;
    }

    private String getAverageScore(List<Manhwa> entries, PreferenceProfile profile) {
        double scoreTotal = 0;
        int ratedCount = 0;
        for (Manhwa manhwa : entries) {
            if (manhwa.isRated()) {
                scoreTotal += manhwa.getOverallScore(profile);
                ratedCount++;
            }
        }
        if (ratedCount == 0) {
            return NO_RATED_ENTRIES_MESSAGE;
        }
        return AVERAGE_SCORE_PREFIX + formatDecimal(scoreTotal / ratedCount);
    }

    private String getAverageRatings(List<Manhwa> entries) {
        StringJoiner averages = new StringJoiner(ITEM_SEPARATOR);
        for (Aspect aspect : Aspect.values()) {
            addAspectAverage(averages, entries, aspect);
        }
        return addItems(AVERAGE_RATINGS_PREFIX, averages);
    }

    private void addAspectAverage(
            StringJoiner averages, List<Manhwa> entries, Aspect aspect) {
        int ratingTotal = 0;
        int ratingCount = 0;
        for (Manhwa manhwa : entries) {
            Integer rating = manhwa.getRating(aspect);
            if (rating != null) {
                ratingTotal += rating;
                ratingCount++;
            }
        }
        if (ratingCount > 0) {
            averages.add(aspect.getDisplayName() + " "
                    + formatDecimal((double) ratingTotal / ratingCount));
        }
    }

    private int getTotalChapters(List<Manhwa> entries) {
        int totalChapters = 0;
        for (Manhwa manhwa : entries) {
            totalChapters += manhwa.getCurrentChapter();
        }
        return totalChapters;
    }

    private String getTopScores(ManhwaList list, PreferenceProfile profile) {
        List<Manhwa> sortedEntries = list.sortedView(SortKey.SCORE, profile);
        StringJoiner topScores = new StringJoiner(ITEM_SEPARATOR);
        int position = FIRST_ENTRY_INDEX;
        for (Manhwa manhwa : sortedEntries) {
            if (manhwa.isRated() && position <= TOP_LIMIT) {
                topScores.add(position + ". " + manhwa.getTitle() + " ("
                        + formatDecimal(manhwa.getOverallScore(profile)) + ")");
                position++;
            }
        }
        return addItems(TOP_SCORES_PREFIX, topScores);
    }

    private String getTopTags(List<Manhwa> entries) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Manhwa manhwa : entries) {
            for (String tag : manhwa.getTags()) {
                counts.merge(tag, COUNT_INCREMENT, Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> sortedTags = new ArrayList<>(counts.entrySet());
        sortedTags.sort(Comparator.comparingInt(
                (Map.Entry<String, Integer> entry) -> entry.getValue()).reversed());
        StringJoiner topTags = new StringJoiner(ITEM_SEPARATOR);
        for (int index = 0; index < Math.min(TOP_LIMIT, sortedTags.size()); index++) {
            Map.Entry<String, Integer> tag = sortedTags.get(index);
            topTags.add("#" + tag.getKey() + " (" + tag.getValue() + ")");
        }
        return addItems(TOP_TAGS_PREFIX, topTags);
    }

    private String getTopPriorities(PreferenceProfile profile) {
        if (profile == null) {
            return NO_PROFILE_MESSAGE;
        }
        List<Aspect> aspects = new ArrayList<>();
        for (Aspect aspect : Aspect.values()) {
            aspects.add(aspect);
        }
        aspects.sort(Comparator.comparingInt(
                (Aspect aspect) -> profile.getWeight(aspect)).reversed());
        StringJoiner priorities = new StringJoiner(ITEM_SEPARATOR);
        for (int index = 0; index < Math.min(TOP_LIMIT, aspects.size()); index++) {
            Aspect aspect = aspects.get(index);
            priorities.add(aspect.getDisplayName() + " (" + profile.getWeight(aspect) + ")");
        }
        return addItems(TOP_PRIORITIES_PREFIX, priorities);
    }

    private String addItems(String prefix, StringJoiner items) {
        assert prefix != null;
        assert items != null;
        if (items.length() == 0) {
            return prefix;
        }
        return prefix + " " + items;
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, ONE_DECIMAL_FORMAT, value);
    }
}
