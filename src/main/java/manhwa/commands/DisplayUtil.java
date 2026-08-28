package manhwa.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import manhwa.Manhwa;
import manhwa.PreferenceProfile;

final class DisplayUtil {
    private static final String FIELD_SEPARATOR = "  ";
    private static final String TAG_PREFIX = "#";
    private static final String SCORE_PREFIX = "Score: ";
    private static final String UNAVAILABLE_DISPLAY = "—";

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
            String header, List<Manhwa> entries, PreferenceProfile profile) {
        assert header != null;
        assert entries != null;
        StringBuilder result = new StringBuilder(header);
        for (int index = 0; index < entries.size(); index++) {
            result.append(System.lineSeparator())
                    .append(index + 1)
                    .append(". ")
                    .append(formatEntry(entries.get(index), profile));
        }
        return result.toString();
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
