package com.swe.aiinsights.request;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a request to generate a summary from chat JSON.
 * Stores the chat input and the prompt used by the AI model.
 */
public final class AiSummarisationRequest implements AiRequestable<String> {

    /**
     * Stores metadata including chat input and prompt.
     */
    private final Map<String, String> metaData;

    /**
     * Stores the request type. For summarisation, this is "SUM".
     */
    private final String type;

    /**
     * Constructs a summarisation request.
     *
     * @param chatJson the raw chat JSON text to be summarised
     */
    public AiSummarisationRequest(final String chatJson) {
        this.metaData = new HashMap<>();
        this.metaData.put("InputChat", chatJson);
        this.metaData.put("RequestPrompt", "give summary");

        this.type = "SUM";
    }

    /**
     * Returns the prompt used for summarisation.
     *
     * @return the prompt text
     */
    @Override
    public String getContext() {
        return this.metaData.get("RequestPrompt");
    }

    /**
     * Returns the raw chat JSON that must be summarised.
     *
     * @return chat input text
     */
    @Override
    public String getInput() {
        return this.metaData.get("InputChat");
    }

    /**
     * Returns the request type ("SUM").
     *
     * @return request type string
     */
    @Override
    public String getReqType() {
        return this.type;
    }
}
