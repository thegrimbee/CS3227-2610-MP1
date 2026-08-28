package manhwa;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * Stores the user's importance weight for every rating aspect.
 */
public class PreferenceProfile {
    private static final String FILE_TYPE = "PREF";
    private static final String FIELD_SEPARATOR = " | ";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String IMPORTANCE_ERROR_MESSAGE =
            "Importance must be an integer from 1 to 5.";
    private static final int MIN_IMPORTANCE = 1;
    private static final int MAX_IMPORTANCE = 5;
    private static final int DEFAULT_IMPORTANCE = MIN_IMPORTANCE;
    private static final int PROFILE_FIELD_COUNT = 6;

    private final Map<Aspect, Integer> weights;

    /**
     * Creates a profile with a valid default weight for every aspect.
     */
    public PreferenceProfile() {
        weights = new EnumMap<>(Aspect.class);
        for (Aspect aspect : Aspect.values()) {
            weights.put(aspect, DEFAULT_IMPORTANCE);
        }
    }

    /**
     * Sets the importance weight for an aspect.
     *
     * @param aspect aspect whose weight should change
     * @param weight importance from 1 to 5
     * @throws ManhwaTrackerException if the importance is outside the allowed range
     */
    public void setWeight(Aspect aspect, int weight) throws ManhwaTrackerException {
        assert aspect != null;
        if (weight < MIN_IMPORTANCE || weight > MAX_IMPORTANCE) {
            throw new ManhwaTrackerException(IMPORTANCE_ERROR_MESSAGE);
        }
        weights.put(aspect, weight);
    }

    /**
     * Returns the importance weight for an aspect.
     *
     * @param aspect aspect whose weight is requested
     * @return importance weight from 1 to 5
     */
    public int getWeight(Aspect aspect) {
        assert aspect != null;
        return weights.get(aspect);
    }

    /**
     * Serializes all aspect weights as one pipe-delimited storage line.
     *
     * @return the storage line
     */
    public String toFileString() {
        StringBuilder result = new StringBuilder(FILE_TYPE);
        for (Aspect aspect : Aspect.values()) {
            result.append(FIELD_SEPARATOR)
                    .append(aspect.getDisplayName())
                    .append(KEY_VALUE_SEPARATOR)
                    .append(weights.get(aspect));
        }
        return result.toString();
    }

    /**
     * Parses one pipe-delimited preference storage line.
     *
     * @param line storage line to parse
     * @return the parsed preference profile
     * @throws ManhwaTrackerException if the line is malformed
     */
    public static PreferenceProfile fromFileString(String line) throws ManhwaTrackerException {
        assert line != null;
        String[] fields = line.split("\\|", -1);
        if (fields.length != PROFILE_FIELD_COUNT || !FILE_TYPE.equals(fields[0].trim())) {
            throw malformedLine(line);
        }

        PreferenceProfile profile = new PreferenceProfile();
        EnumSet<Aspect> parsedAspects = EnumSet.noneOf(Aspect.class);
        try {
            for (int index = 1; index < fields.length; index++) {
                parseWeight(fields[index].trim(), profile, parsedAspects, line);
            }
        } catch (NumberFormatException exception) {
            throw malformedLine(line);
        }
        if (parsedAspects.size() != Aspect.values().length) {
            throw malformedLine(line);
        }
        return profile;
    }

    private static void parseWeight(String value, PreferenceProfile profile,
            EnumSet<Aspect> parsedAspects, String line) throws ManhwaTrackerException {
        String[] weightParts = value.split(KEY_VALUE_SEPARATOR, -1);
        if (weightParts.length != 2) {
            throw malformedLine(line);
        }
        Aspect aspect = Aspect.fromString(weightParts[0].trim());
        if (!parsedAspects.add(aspect)) {
            throw malformedLine(line);
        }
        profile.setWeight(aspect, Integer.parseInt(weightParts[1].trim()));
    }

    private static ManhwaTrackerException malformedLine(String line) {
        return new ManhwaTrackerException("Malformed preference data: " + line);
    }
}
