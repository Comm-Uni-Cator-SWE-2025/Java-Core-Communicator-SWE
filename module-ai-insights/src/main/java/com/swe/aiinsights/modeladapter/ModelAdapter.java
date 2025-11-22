package com.swe.aiinsights.modeladapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import okhttp3.Response;

import java.io.IOException;

/**
 * Interface for the adapters of AI models.
 */
public interface ModelAdapter {

    /**
     * Builds the request string specific to the model.
     * @param req the generalised request
     * @return a string - json format of the request specific to that model
     * @throws JsonProcessingException in case of an error in processing json
     */
    String buildRequest(RequestGeneraliser req) throws JsonProcessingException;

    /**
     * Fetches the text response at a location in the AI response json specific to AI model.
     * @param response response from AI.
     * @return text which AI gave as the response.
     * @throws IOException in case of no text in the response.
     */
    String getResponse(Response response) throws IOException;
}
