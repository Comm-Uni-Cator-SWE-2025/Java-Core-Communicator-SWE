/*
 * -----------------------------------------------------------------------------
 *  File: RegulariserResponse.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.response
 * -----------------------------------------------------------------------------
 */

/**
 * Author : Abhirami R Iyer.
 * <p>
 *  Holds the AI output for regularisation requests.
 *  Stores the processed content returned by the model.
 *  </p>
 */

package com.swe.aiinsights.response;

import com.swe.aiinsights.logging.CommonLogger;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * RegulariserResponse holds the response of AI regularisation.
 */
public class RegulariserResponse implements AiResponse {
    /**
     * Get the log file path.
     */
    private static final Logger LOG =
            CommonLogger.getLogger(RegulariserResponse.class);
    /**
     * Type stores the type of the particular response.
     */
    private String type;

    /**
     *  metadata would contain the response details.
     */
    private Map<String, String> metaData;

    /**
     * Constructs an InterpreterResponse and initializes it to a default type.
     */
    public RegulariserResponse() {
        LOG.info("Creating regulariser response");
        type = "Regulariser Response";
        metaData = new HashMap<>();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse() {
        // to return the contents of the response
        LOG.info("Fetching response from Regulariser response");
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
        LOG.info("Setting response in RegulariserResponse");
        metaData.put("Content", content);
    }





}
