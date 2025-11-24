/**
 * Class that handles insights response creation.
 * @author Nandhana Sunil
 * @version 1.0.0
 * @since 1.0.0
 */

package com.swe.aiinsights.response;

import java.util.HashMap;
import java.util.Map;

/**
 * Insights response, represents the tuple (time, value of sentiment (float)).
 */
public class InsightsResponse implements AiResponse {
    /**
     * Type stores the type of the particular response.
     */
    private final String type;

    /**
     *  metadata would contain the response details.
     */
    private final Map<String, String> metaData;

    /**
     * Constructs an InterpreterResponse and initializes it to a default type.
     */
    public InsightsResponse() {
        type = "Insights graph points";
        metaData = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse() {
        // to return the contents of the response
        return metaData.get("Content");
    }

    /**
     * {@inheritDoc}
     *
     * @param content the content to set in the response
     */
    @Override
    public void setResponse(final String content) {
        // to set the content in case of receiving response from the Ai model
        metaData.put("Content", content);
    }
}
