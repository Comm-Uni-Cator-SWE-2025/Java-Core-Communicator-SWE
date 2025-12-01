/*
 * -----------------------------------------------------------------------------
 *  File: AiInsightsRequest.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights.request
 * -----------------------------------------------------------------------------
 */

/**
 * Class that handles insights generation requests to AI.
 * <p>
 *     This class is used to navigate the requests for insights generation to
 *     an LLM service. It gets the chat data as a json file, sends it to
 *     LLM for sentiment analysis and gets the values as a list of floats.
 *     The results are then plotted in the insights tab.
 * </p>
 *
 * <p>
 *     References
 *     1. https://stackoverflow.com/questions/32875874
 *     /get-key-name-key-value-from-json
 *     2. https://www.geeksforgeeks.org/java/
 *     java-util-hashmap-in-java-with-examples/
 *     3. https://www.javaspring.net/blog/
 *     convert-object-to-jsonobject-java/
 * </p>
 * @author Nandhana Sunil
 * @version 1.0.0
 * @since 1.0.0
 */

package com.swe.aiinsights.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AiInsightsRequest class inherits the IAIRequest.
 * Stores the metadata of the request to be made to the AI.
 */
public class AiInsightsRequest implements AiRequestable<JsonNode> {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG =
            SweLoggerFactory.getLogger("AI-INSIGHTS");
    /**
     * metaDataInsight stores the prompt.
     * Also, other details of the request like the content.
     */
    private final Map<String, Object> metaDataInsight;
    /**
     * type stores the type of request .
     * type = "INS"
     */
    private final String type;
    /**
     * Constructs an AiInsightsRequest,
     * Initialises the metaDataInsight with a default prompt,
     * to generate sentiment value.
     * @param chatData will be a json object with the chat messages
     */

    public AiInsightsRequest(final JsonNode chatData) throws IOException {
        // Initialises the metaDataInsight with prompt and data.
        LOG.info("Creating insights Request..");
        metaDataInsight = new HashMap<>();
        metaDataInsight.put("InputChatData", chatData);
        metaDataInsight.put("RequestPrompt", """
            You are performing sentiment analysis on a
            chronological chat conversation.
    
            For each message in the chat:
            - Determine the sentiment on a scale from -10.0 to +10.0
              where -1.0 = very negative, 0 = neutral, and +1.0 = very positive.
            - Use only the "message" field to determine sentiment.
            - Preserve the precise timestamp associated with each message.
    
            Return the output as a JSON array of objects in the exact format below,
            without any additional commentary or explanation:
    
            [
              {
                "time": "<timestamp>",
                "sentiment": <float>
              },
              ...
            ]
            """);
        type = "INS";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContext() {
        // Returns the request prompt.
        LOG.info("Fetching Insights Request prompt..");
        return metaDataInsight.get("RequestPrompt").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReqType() {
        // returns "INS".
        LOG.info("Fetching Request type..");
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode getInput() {
        // this function returns the input.
        LOG.info("Fetching Insights Request input data..");
        return (JsonNode) metaDataInsight.get("InputChatData");
    }
}
