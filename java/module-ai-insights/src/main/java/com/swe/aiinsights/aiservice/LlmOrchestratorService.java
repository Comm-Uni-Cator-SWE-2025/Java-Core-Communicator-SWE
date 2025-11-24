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

import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.response.AiResponse;
import java.io.IOException;
import java.util.List;
import com.swe.aiinsights.customexceptions.RateLimitException;
import com.swe.aiinsights.logging.CommonLogger;
import org.slf4j.Logger;

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
    private static final Logger LOG = CommonLogger.getLogger(LlmOrchestratorService.class);

    /**
     * This constructor takes a list of LlmService instances in the order of execution needed.
     * @param services all the services available
     */
    public LlmOrchestratorService(final List<LlmService> services) {
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("Provide a list of LLM Services !!!!");
        }
        this.llmServices = services;
        LOG.info("LlmOrchestratorService initialized with {} services", services.size());
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

//            System.out.println("INFO: Attempting service: "
//                    + serviceName + " for request: " + request.getReqType());
            LOG.info("Attempting service: {} for request: {}", serviceName, request.getReqType());
            
            try {
                // process the request with the current service
                final AiResponse response = currentService.runProcess(request);
                
                // If successful, update the active index if a switch occurred
                if (i != startingIndex) {
                    // Switch was successful, permanently use this new service
                    activeServiceIndex = i; 
//                    System.out.println("SUCCESS: Permanently switched to service: " + serviceName);
                    LOG.info("Permanently switched to service: {}", serviceName);
                }
                return response;

            } catch (RateLimitException e) {
                // Rate limit exception is hit, move to next service in the list.
//                System.err.println("WARNING: Service " + serviceName +
//                        " failed due to rate limit. Attempting next service.");
                LOG.warn("Service {} failed due to rate limit. Attempting next service.", serviceName);

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