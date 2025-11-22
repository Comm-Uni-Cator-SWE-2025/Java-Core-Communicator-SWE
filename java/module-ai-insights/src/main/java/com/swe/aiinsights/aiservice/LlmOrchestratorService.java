/**
 * Switches between cloud models and local LLMs when rate limit is hit.
 * 
 * <p>
 *     References :
 * </p>
 *
 * @author Nandhana Sunil
 * @version 1.0.0
 * @since 1.0.0
 */


package com.swe.aiinsights.aiservice;

import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.response.AiResponse;
import java.io.IOException;
import java.util.List;
import com.swe.aiinsights.customexceptions.RateLimitException;

// Acts as an orchestrator to provide an LLM Service
public class LlmOrchestratorService implements LlmService {

    private final List<LlmService> llmServices; // List of all LLMs in order of preference
    private volatile int activeServiceIndex = 0; // Index of the currently used service

    /**
     * This constructor takes a list of LlmService instances in the order
     * that we need to execute.
     */
    public LlmOrchestratorService(List<LlmService> services) {
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("Provide a list of LLM Services !!!!");
        }
        this.llmServices = services;
    }

    /**
     * Tries the active a service. If it fails due to a RateLimitException,
     * it switches to the next service in the list and retries the request.
     */
    @Override
    public AiResponse runProcess(final RequestGeneraliser request) throws IOException {
        
        // Start from the currently active service index
        int startingIndex = activeServiceIndex;
        
        for (int i = startingIndex; i < llmServices.size(); i++) {
            LlmService currentService = llmServices.get(i);
            String serviceName = currentService.getClass().getSimpleName();

            System.out.println("INFO: Attempting service: "
                    + serviceName + " for request: " + request.getReqType());
            
            try {
                // process the request with the current service
                AiResponse response = currentService.runProcess(request);
                
                // If successful, update the active index if a switch occurred
                if (i != startingIndex) {
                    // Switch was successful, permanently use this new service
                    activeServiceIndex = i; 
                    System.out.println("SUCCESS: Permanently switched to service: " + serviceName);
                }
                return response;

            } catch (RateLimitException e) {
                // Rate limit exception is hit, move to next service in the list.
                System.err.println("WARNING: Service " + serviceName +
                        " failed due to rate limit. Attempting next service.");

                // Here we set the last AI service to local LLM
                // It will not throw Rate limit exception.
            }
        }
        
        // Should be unreachable, because last one will be local LLM
        // but included for safety incase any other exception occurs.
        throw new IOException("All configured LLM services failed to process the request.");
    }
}