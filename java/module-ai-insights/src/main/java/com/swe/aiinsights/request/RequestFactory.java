/**
 * Factory to generate various kinds of requests.
 *
 * @author Abhirami R Iyer
 *
 */

package com.swe.aiinsights.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.swe.aiinsights.data.WhiteBoardData;

import java.io.IOException;

/**
 * Class factory to generate various requests.
 */
public class RequestFactory {

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
                final String filePath = (String) args[0];
                final WhiteBoardData data = new WhiteBoardData(filePath);
                request = new AiDescriptionRequest(data);
                break;
            case "REG" :
                final String points = (String) args[0];
                request = new AiRegularisationRequest(points);
                break;
            case "INS" :
                final JsonNode chatData = (JsonNode) args[0];
                request = new AiInsightsRequest(chatData);
                break;
            case "SUM" :
                final String contentToSummarise = (String) args[0];
                request = new AiSummarisationRequest(contentToSummarise);
                break;
            case "ACTION" :
                final JsonNode chat = (JsonNode) args[0];
                request = new AiActionItemsRequest(chat);
                break;
            case "QNA" :
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
