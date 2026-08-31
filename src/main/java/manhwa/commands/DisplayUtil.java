package manhwa.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import manhwa.Aspect;
import manhwa.Manhwa;
import manhwa.ManhwaList;
import manhwa.PreferenceProfile;

final class DisplayUtil {
    private static final String FIELD_SEPARATOR = "  ";
    private static final String TAG_PREFIX = "#";
    private static final String SCORE_PREFIX = "Score: ";
    private static final String UNAVAILABLE_DISPLAY = "-";
    private static final String DETAIL_INDENT = "   ";
    private static final String BREAKDOWN_INDENT = "      ";

    private DisplayUtil() {
    }

    static String formatEntry(Manhwa manhwa, PreferenceProfile profile) {
        assert manhwa != null;
        List<String> fields = new ArrayList<>();
        fields.add("[" + manhwa.getStatus().name() + "] " + manhwa.getTitle());
        if (!manhwa.getTags().isEmpty()) {
            fields.add(formatTags(manhwa));
        }
        fields.add(manhwa.getChapterDisplay());
        fields.add(SCORE_PREFIX + formatScore(manhwa, profile));
        return String.join(FIELD_SEPARATOR, fields);
    }

    static String formatEntries(
            String header, List<Manhwa> entries, PreferenceProfile profile,
            ManhwaList sourceList) {
        assert header != null;
        assert entries != null;
        assert sourceList != null;
        StringBuilder result = new StringBuilder(header);
        for (Manhwa entry : entries) {
            int permanentIndex = sourceList.getIndexOf(entry);
            assert permanentIndex > 0;
            result.append(System.lineSeparator())
                    .append(permanentIndex)
                    .append(". ")
                    .append(formatEntry(entry, profile));
        }
        return result.toString();
    }

    static String formatDetailedEntries(
            String header, List<Manhwa> entries, PreferenceProfile profile,
            ManhwaList sourceList) {
        assert header != null;
        assert entries != null;
        assert sourceList != null;
        StringBuilder result = new StringBuilder(header);
        for (int index = 0; index < entries.size(); index++) {
            Manhwa entry = entries.get(index);
            int permanentIndex = sourceList.getIndexOf(entry);
            assert permanentIndex > 0;
            result.append(System.lineSeparator())
                    .append(permanentIndex)
                    .append(". ")
                    .append(formatDetailedEntry(entry, profile));
            if (index < entries.size() - 1) {
                result.append(System.lineSeparator());
            }
        }
        return result.toString();
    }

    private static String formatDetailedEntry(
            Manhwa manhwa, PreferenceProfile profile) {
        assert manhwa != null;
        String lineSeparator = System.lineSeparator();
        String tags = manhwa.getTags().isEmpty()
                ? UNAVAILABLE_DISPLAY : formatTags(manhwa);
        String note = manhwa.getNote() == null
                ? UNAVAILABLE_DISPLAY : manhwa.getNote();
        StringBuilder result = new StringBuilder(manhwa.getTitle())
                .append(lineSeparator).append(DETAIL_INDENT)
                .append("Status: ").append(manhwa.getStatus().getDisplayName())
                .append(lineSeparator).append(DETAIL_INDENT)
                .append("Date added: ").append(manhwa.getDateAdded())
                .append(lineSeparator).append(DETAIL_INDENT)
                .append("Chapters: ").append(manhwa.getChapterDisplay())
                .append(lineSeparator).append(DETAIL_INDENT)
                .append("Tags: ").append(tags)
                .append(lineSeparator).append(DETAIL_INDENT)
                .append("Note: ").append(note)
                .append(lineSeparator).append(DETAIL_INDENT)
                .append("Score breakdown:");
        appendScoreBreakdown(result, manhwa, profile);
        return result.toString();
    }

    private static void appendScoreBreakdown(
            StringBuilder result, Manhwa manhwa, PreferenceProfile profile) {
        assert result != null;
        assert manhwa != null;
        int weightedTotal = 0;
        int weightTotal = 0;
        for (Aspect aspect : Aspect.values()) {
            Integer rating = manhwa.getRating(aspect);
            result.append(System.lineSeparator()).append(BREAKDOWN_INDENT)
                    .append(aspect.getDisplayName())
                    .append(": rating ")
                    .append(rating == null ? UNAVAILABLE_DISPLAY : rating)
                    .append(", weight ");
            if (profile == null) {
                result.append(UNAVAILABLE_DISPLAY)
                        .append(", contribution ").append(UNAVAILABLE_DISPLAY);
            } else {
                int weight = profile.getWeight(aspect);
                result.append(weight).append(", contribution ");
                if (rating == null) {
                    result.append(UNAVAILABLE_DISPLAY);
                } else {
                    int contribution = rating * weight;
                    weightedTotal += contribution;
                    weightTotal += weight;
                    result.append(contribution);
                }
            }
        }
        result.append(System.lineSeparator()).append(DETAIL_INDENT)
                .append("Overall score: ");
        if (profile == null || weightTotal == 0) {
            result.append(UNAVAILABLE_DISPLAY);
        } else {
            result.append(weightedTotal).append(" / ").append(weightTotal)
                    .append(" = ").append(formatScore(manhwa, profile));
        }
    }

    private static String formatTags(Manhwa manhwa) {
        List<String> displayedTags = new ArrayList<>();
        for (String tag : manhwa.getTags()) {
            displayedTags.add(TAG_PREFIX + tag);
        }
        return String.join(" ", displayedTags);
    }

    private static String formatScore(Manhwa manhwa, PreferenceProfile profile) {
        if (profile == null) {
            return UNAVAILABLE_DISPLAY;
        }
        double score = manhwa.getOverallScore(profile);
        if (score < 0) {
            return UNAVAILABLE_DISPLAY;
        }
        return String.format(Locale.ROOT, "%.1f", score);
    }
}
