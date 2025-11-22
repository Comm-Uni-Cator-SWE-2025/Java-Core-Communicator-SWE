/**
 * Author : Abhirami R Iyer
 */
package com.swe.aiinsights.aiservice;

import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.response.AiResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


/**
 * Interface for the llm services.
 */
public interface LlmService {
    /**
     * Function to send the request to AI api and get the response.
     * @param request holds the request of the user
     * @return AiResponse, returns the response from AI
     * @throws IOException
     */
    AiResponse runProcess(RequestGeneraliser request) throws IOException;
}
