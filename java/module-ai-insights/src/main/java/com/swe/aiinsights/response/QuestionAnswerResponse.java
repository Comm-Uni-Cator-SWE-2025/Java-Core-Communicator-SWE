/*
 * -----------------------------------------------------------------------------
 *  File: QuestionAnswerResponse.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201003
 *  Module : com.swe.aiinsights.response
 * -----------------------------------------------------------------------------
 */

/**
 * Author Berelli Gouthami.
 */

package com.swe.aiinsights.response;

import com.swe.aiinsights.logging.CommonLogger;
import org.slf4j.Logger;

/**
 * Represents the AI's response to a Question & Answer request.
 */
public class QuestionAnswerResponse implements AiResponse {
    /**
     * Get the log file path.
     */
    private static final Logger LOG =
            CommonLogger.getLogger(QuestionAnswerResponse.class);

    /**
     * Stores the response text returned by the AI.
     */
    private String responseText;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResponse(final String text) {
        LOG.info("Q&A response updated");
        this.responseText = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse() {
        LOG.info("Q&A response retrieved");
        return this.responseText;
    }
}