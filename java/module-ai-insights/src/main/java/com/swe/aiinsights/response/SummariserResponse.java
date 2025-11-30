/*
 * -----------------------------------------------------------------------------
 *  File: SummariserResponse.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201003
 *  Module : com.swe.aiinsights.response
 * -----------------------------------------------------------------------------
 */

/**
 * Author Berelli Gouthami.
 */

package com.swe.aiinsights.response;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

/**
 * Represents the AI's response to a chat summarisation request.
 * This class stores the summarised text returned by the model and
 * identifies the response type for downstream handling.
 */
public class SummariserResponse implements AiResponse {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG =
            SweLoggerFactory.getLogger("AI-INSIGHTS");

    /**
     * The summary text returned by the AI model.
     */
    private String responseText;

    /**
     * Sets the summarised response text.
     *
     * @param text the text returned by the AI
     */
    @Override
    public void setResponse(final String text) {
        LOG.info("Summary response updated");
        this.responseText = text;
    }

    /**
     * Returns the summarised response text.
     *
     * @return the AI-generated summary
     */
    @Override
    public String getResponse() {
        LOG.debug("Returning stored summary response");
        return this.responseText;
    }

    /**
     * Provides direct access to the summary text.
     * This is a helper method for readability
     * @return the AI-generated summary
     */
    public String getResponseText() {
        LOG.info("Summary retrieved for further processing");
        return responseText;
    }
}