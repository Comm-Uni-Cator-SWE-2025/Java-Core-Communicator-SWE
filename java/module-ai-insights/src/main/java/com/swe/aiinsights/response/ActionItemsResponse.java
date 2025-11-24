/*
 * -----------------------------------------------------------------------------
 *  File: ActionItemsResponse.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights.response
 * -----------------------------------------------------------------------------
 */

/**
 * Class that handles action items response creation.
 * @author Nandhana Sunil
 * @version 1.0.0
 * @since 1.0.0
 */

package com.swe.aiinsights.response;

import com.swe.aiinsights.logging.CommonLogger;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Action items response is a list of strigs.
 */
public class ActionItemsResponse implements AiResponse {
    /**
     * Get the log file path.
     */
    private static final Logger LOG =
            CommonLogger.getLogger(ActionItemsResponse.class);
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
    public ActionItemsResponse() {
        LOG.info("Creating Action items response");
        type = "Action Items list";
        metaData = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse() {
        // to return the contents of the response
        LOG.info("Fetching response from ActionItemsResponse");
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
        LOG.info("Setting response in ActionItemsResponse");
        metaData.put("Content", content);
    }
}
