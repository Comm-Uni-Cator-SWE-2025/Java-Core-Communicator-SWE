/*
 * -----------------------------------------------------------------------------
 *  File: AsyncAiExecutor.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.apiendpoints
 * -----------------------------------------------------------------------------
 */

/**
 * Asynchronous execution of various requests is handled here.
 *
 * @author Abhirami R Iyer
 * @editedby Nandhana Sunil
 */

package com.swe.aiinsights.apiendpoints;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.swe.aiinsights.aiservice.GeminiService;
import com.swe.aiinsights.aiservice.LlmService;
import com.swe.aiinsights.aiservice.OllamaService;
import com.swe.aiinsights.aiservice.LlmOrchestratorService;
import com.swe.aiinsights.configu.AsyncConfig;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.response.AiResponse;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Handles asynchronous execution of AI requests using the LLM orchestrator.
 */
public class AsyncAiExecutor {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("AI-INSIGHTS");

    /**
     * Shared async executor for running AI tasks.
     */
    private static final Executor AI_EXECUTOR = AsyncConfig.aiExecutor();
    /**
     * Orchestrates between Gemini and Ollama services.
     */
    private final LlmService llmService = new LlmOrchestratorService(
        List.of(
            new GeminiService(), // 1. Primary
            new OllamaService() // 2. Fallback
        )
    );

    /**
     * Executes an AI request asynchronously and returns the model output as a future.
     *
     * @param req the AI request object
     * @return future containing the AI model's response string
     */
    public CompletableFuture<String> execute(final AiRequestable req) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.debug("Creating RequestGeneralised...");
                final RequestGeneraliser general = new RequestGeneraliser(req);

                LOG.debug("Calling llmService.runProcess()...");
                final AiResponse aiResponse = llmService.runProcess(general);

                final String response = general.formatOutput(aiResponse);
                LOG.debug("Received response");
                return response;

            }  catch (IOException e) {
                LOG.error("IOException in execute: " + e.getMessage(), e);
                throw new RuntimeException(e);
                } catch (Exception e) {
                    LOG.error("Unexpected exception in execute: " + e.getMessage(), e);
                    throw new RuntimeException(e);
                }

            }, AI_EXECUTOR
        );
    }

}
