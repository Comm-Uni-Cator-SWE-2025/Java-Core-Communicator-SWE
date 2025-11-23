/**
 * Interface for various service AI service modules.
 *
 * @author Abhirami R Iyer
 */

package com.swe.aiinsights.aiservice;

import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.response.AiResponse;
import java.io.IOException;



/**
 * Interface for the llm services.
 */
public interface LlmService {
    /**
     * Function to send the request to AI api and get the response.
     * @param request holds the request of the user
     * @return AiResponse, returns the response from AI
     * @throws IOException in case of error in reading AI response
     */
    AiResponse runProcess(RequestGeneraliser request) throws IOException;
}
