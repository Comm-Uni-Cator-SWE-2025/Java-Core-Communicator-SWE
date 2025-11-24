/*
 * -----------------------------------------------------------------------------
 *  File: RequestFactory.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.request
 * -----------------------------------------------------------------------------
 */


package com.swe.aiinsights.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.swe.aiinsights.data.WhiteBoardData;
import com.swe.aiinsights.logging.CommonLogger;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Class factory to generate various requests.
 */
public class RequestFactory {
    /**
     * Get the log file path.
     */
    private static final Logger LOG =
            CommonLogger.getLogger(RequestFactory.class);

    /**
     * Returns the AiRequestable corresponding to the type of request.
     *
     * @param requestType the kind of request given
     * @param args all arguments required for creating a particular request
     * @return the AiRequestable
     * @throws IOException in case of any I/O exception
     */
    public AiRequestable getRequest(final String requestType, final Object... args) throws IOException {
        final AiRequestable request;
        switch (requestType) {
            case "DESC" :
                LOG.info("Creating description request");
                final WhiteBoardData data = (WhiteBoardData) args[0];
                request = new AiDescriptionRequest(data);
                break;
            case "REG" :
                LOG.info("Creating regularisation request");
                final String points = (String) args[0];
                request = new AiRegularisationRequest(points);
                break;
            case "INS" :
                LOG.info("Creating insights request");
                final JsonNode chatData = (JsonNode) args[0];
                request = new AiInsightsRequest(chatData);
                break;
            case "SUM" :
                LOG.info("Creating summarization request");
                final String contentToSummarise = (String) args[0];
                request = new AiSummarisationRequest(contentToSummarise);
                break;
            case "ACTION" :
                LOG.info("Creating action request");
                final JsonNode chat = (JsonNode) args[0];
                request = new AiActionItemsRequest(chat);
                break;
            case "QNA" :
                LOG.info("Creating question and answer request");
                final String question = (String) args[0];
                final String accumulatedSummary = (String) args[1];
                request = new AiQuestionAnswerRequest(question, accumulatedSummary);
                break;
            default:
                request = null;
                break;
        }

        return request;
    }
}
