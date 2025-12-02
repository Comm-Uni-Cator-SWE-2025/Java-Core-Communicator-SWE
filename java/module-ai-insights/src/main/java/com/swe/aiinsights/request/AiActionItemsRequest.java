/*
 * -----------------------------------------------------------------------------
 *  File: AiActionItemsRequest.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights.request
 * -----------------------------------------------------------------------------
 */

/**
 * Class that handles action item generation requests to AI.
 * <p>
 *     This class is used to navigate the requests for action items generation
 *     to an LLM service. It gets the chat data as a json file, sends it to
 *     LLM for identifies the action items and gets the values as a list of text.
 * </p>
 *
 * @author Nandhana Sunil
 * @version 1.0.0
 * @since 1.0.0
 */

package com.swe.aiinsights.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AiActionItemsRequest class inherits the AiRequestable.
 * Stores the metadata of the request to be made to the AI.
 */
public class AiActionItemsRequest implements AiRequestable<JsonNode> {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG =
            SweLoggerFactory.getLogger("AI-INSIGHTS");
    /**
     * metaData stores the prompt.
     * Also, other details of the request like the content.
     */
    private final Map<String, Object> metaData;
    /**
     * type stores the type of request .
     * type = "ACTION"
     */
    private final String type;
    /**
     * Constructs an AiActionItemsRequest,
     * Initialises the metaData with a default prompt,
     * to identify action items.
     * @param chatData will be a json object with the chat messages
     */

    public AiActionItemsRequest(final JsonNode chatData) throws IOException {
        // Initialises the metaData with prompt and data.
        LOG.info("Creating new ActionItems request");
        metaData = new HashMap<>();
        metaData.put("InputChatData", chatData);
        metaData.put("RequestPrompt", """
            From the following chat transcript, 
            identify only the most important and concrete action items.
            Rewrite each as a short, clear statement in the third person,
            using as few words as possible while keeping full meaning.
            Exclude general discussions, suggestions, or decisions —
            include only actions that someone explicitly commits to doing.
            Return the output strictly as a JSON list of strings — nothing else.
            """);
        type = "ACTION";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContext() {
        // Returns the request prompt.
        LOG.info("Fetching ActionItems request prompt");
        return metaData.get("RequestPrompt").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReqType() {
        // returns "INS".
        LOG.info("Fetching request type");
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode getInput() {
        // this function returns the input.
        LOG.info("Fetching ActionItems input data");
        return (JsonNode) metaData.get("InputChatData");
    }
}
