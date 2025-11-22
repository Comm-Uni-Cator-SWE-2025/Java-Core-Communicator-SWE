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
        final String prompt = """
                You will receive either: (1) just new chat data, 
                or (2) a previous summary followed by new chat data. 
                Your task is to create a concise, cohesive summary that
                captures the key points and important information.
                If there is a previous summary, understand the context from
                it and integrate the new chat information to create 
                an updated overall summary. Do not just append or list things
                - synthesize the information into a natural paragraph.
                Focus on: who participated, main topics discussed,
                important updates, decisions, and action items.
                Keep it brief but ensure no critical information is lost. 
                Write in clear, flowing paragraph format.
                include full summary when you give me output""";
        this.metaData.put("RequestPrompt", prompt);


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
