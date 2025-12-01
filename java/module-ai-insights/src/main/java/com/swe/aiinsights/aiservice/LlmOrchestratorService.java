/*
 * -----------------------------------------------------------------------------
 *  File: LlmOrchestratorService.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights.aiinstance
 * -----------------------------------------------------------------------------
 */

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

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.response.AiResponse;
import java.io.IOException;
import java.util.List;
import com.swe.aiinsights.customexceptions.RateLimitException;
/**
 * Acts as an orchestrator to provide an LLM Service.
 */

public class LlmOrchestratorService implements LlmService {
    /**
     * List of all LLMs in order of preference.
     */
    private final List<LlmService> llmServices;
    /**
     * Index of the currently used service.
     */
    private volatile int activeServiceIndex = 0;

    /**
     * Get the log file path.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("AI-INSIGHTS");

    /**
     * This constructor takes a list of LlmService instances in the order of execution needed.
     * @param services all the services available
     */
    public LlmOrchestratorService(final List<LlmService> services) {
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("Provide a list of LLM Services !!!!");
        }
        this.llmServices = services;
        LOG.info("LlmOrchestratorService initialized with " + services.size() + " services");
    }

    /**
     * Tries the active a service. If it fails due to a RateLimitException,
     * it switches to the next service in the list and retries the request.
     * {@inheritDoc}
     */
    @Override
    public AiResponse runProcess(final RequestGeneraliser request) throws IOException {
        
        // Start from the currently active service index
        final int startingIndex = activeServiceIndex;
        
        for (int i = startingIndex; i < llmServices.size(); i++) {
            final LlmService currentService = llmServices.get(i);
            final String serviceName = currentService.getClass().getSimpleName();

//            LOG.info("INFO: Attempting service: "
//                    + serviceName + " for request: " + request.getReqType());
            LOG.info("Attempting service: " + serviceName + " for request: " + request.getReqType());
            
            try {
                // process the request with the current service
                final AiResponse response = currentService.runProcess(request);
                
                // If successful, update the active index if a switch occurred
                if (i != startingIndex) {
                    // Switch was successful, permanently use this new service
                    activeServiceIndex = i; 
//                    LOG.info("SUCCESS: Permanently switched to service: " + serviceName);
                    LOG.info("Permanently switched to service: " + serviceName);
                }
                return response;

            } catch (RateLimitException e) {
                // Rate limit exception is hit, move to next service in the list.
//                LOG.error("WARNING: Service " + serviceName +
//                        " failed due to rate limit. Attempting next service.");
                LOG.warn("Service " + serviceName + " failed due to rate limit. Attempting next service.");

                // Here we set the last AI service to local LLM
                // It will not throw Rate limit exception.
            }
        }
        
        // Should be unreachable, because last one will be local LLM
        // but included for safety incase any other exception occurs.
        LOG.error("All configured LLM services failed to process the request.");
        throw new IOException("All configured LLM services failed to process the request.");
    }
}