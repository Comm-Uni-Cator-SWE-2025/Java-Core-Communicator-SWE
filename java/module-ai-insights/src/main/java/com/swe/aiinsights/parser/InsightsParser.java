/*
 * -----------------------------------------------------------------------------
 *  File: InsightsParser.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights.parser
 * -----------------------------------------------------------------------------
 */

/**
 * <p>
 *     References:
 *      1. https://www.w3schools.com/java/java_regex.asp
 *      2. https://www.geeksforgeeks.org/java/regular-expressions-in-java/
 * </p>
 *
 * @author Nandhana Sunil
 */

package com.swe.aiinsights.parser;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Method used to parse the output of insights generator.
 */

public class InsightsParser {

    /**
     * Get the log file path.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("AI-INSIGHTS");

    /**
     * Object mapper to map the output to the sentiments class.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * The regular expression pattern to match the expected format.
     * Time is in the format YYYY-MM-DDTHH:MM:SSZ
     */
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$"
    );

    /**
     * Convert the validated list back to a formatted JSON string
     * exactly like the original required output.
     * @param raw the raw output from the LLM.
     * @return raw if it is in the expected format.
     */

    public static String parse(final String raw) throws JsonProcessingException {

        // Remove ``` and ```json
        final String rawCleaned = cleanTicks(raw.trim());

        // Check if string starts with [ and endswith ]
        if (!rawCleaned.startsWith("[") || !rawCleaned.endsWith("]")) {
            LOG.error("LLM output do not start with [ and ]\n");
            throw new IllegalArgumentException("Output not in the expected format");
        }

        // Parse JSON into list of objects
        final CollectionType listType = MAPPER.getTypeFactory()
                .constructCollectionType(List.class, SentimentEntry.class);

        final List<SentimentEntry> entries;
        try {
            entries = MAPPER.readValue(rawCleaned, listType);
        } catch (Exception e) {
            LOG.error("LLM output not in the {sentiment, time} format\n");
            throw new IllegalArgumentException("Output not in the expected format", e);
        }

        for (final SentimentEntry r : entries) {
            validateEntry(r);
        }

        return rawCleaned;
    }

    /**
     * validate if each of entry are in expected format.
     * @param r entry of sentiment value from each chat.
     */
    private static void validateEntry(final SentimentEntry r) {
        if (r.time == null || !TIME_PATTERN.matcher(r.time).matches()) {
            throw new IllegalArgumentException("Invalid time format: " + r.time);
        }

        final int maxValue = 10;
        final int minValue = -10;
        if (r.sentiment < minValue || r.sentiment > maxValue) {
            throw new IllegalArgumentException("Sentiment out of range [-10, 10]: " + r.sentiment);
        }
    }

    /**
     * Used to remove the ``` and ```json from LLM output.
     * @param input the output from the LLM.
     * @return string with ``` and ```json removed.
     */
    private static String cleanTicks(final String input) {
        String s;
        if (input.startsWith("```json")) {
            final int startlen = 7;
            s = input.substring(startlen).trim(); // remove ```json
        } else if (input.startsWith("```")) {
            final int tickstartline = 3;
            s = input.substring(tickstartline).trim(); // remove ```
        } else {
            s = input;
        }

        if (s.endsWith("```")) {
            final int start = 0;
            final int end = 3;
            s = s.substring(start, s.length() - end).trim();
        }
        return s;
    }

    /**
     * Representational class for each of the entry from LLM.
     */
    public static class SentimentEntry {
        /**
         * time field in the entry.
         */
        private String time;
        /**
         * sentiment field.
         */
        private float sentiment;

        public String getTime() {
            return time;
        }

        public void setTime(final String timeValue) {
            this.time = timeValue;
        }

        public float getSentiment() {
            return sentiment;
        }

        public void setSentiment(final float sentimentValue) {
            this.sentiment = sentimentValue;
        }
    }
}
