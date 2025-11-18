/**
 * Author : Abhirami R Iyer
 */
package com.swe.aiinsights.response;

import java.util.HashMap;
import java.util.Map;

/**
 * RegulariserResponse holds the response of AI regularisation.
 */
public class RegulariserResponse implements AiResponse {
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
        type = "Regulariser Response";
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
        // to set the content in case of recieving resposne from the Ai model
        metaData.put("Content", content);
    }





}
