/*
 * -----------------------------------------------------------------------------
 *  File: AiClientService.java
 *  Owner: Abhirami R Iyer, Nandhana Sunil, Berelli Gouthami
 *  Roll Number : 112201001, 112201008, 112201003
 *  Module : com.swe.aiinsights.apiendpoints
 * -----------------------------------------------------------------------------
 */

/**
 * API functions for various AI services.
 *
 * @author Abhirami R Iyer
 * @editedby Nandhana Sunil, Berelli Gouthami
 */

package com.swe.aiinsights.apiendpoints;


import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.swe.aiinsights.data.WhiteBoardData;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.request.RequestFactory;

import java.util.concurrent.CompletableFuture;
import java.io.IOException;

/**
 * AI client service.
 * contains the api endpoints for all the services offered by the AI module.
 */

public class AiClientService {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("AI-INSIGHTS");


    /**
     * Executor used to run async AI calls.
     */
    private static final AsyncAiExecutor ASYNC_AI_EXECUTOR = new AsyncAiExecutor();
    /**
     * Request factory for generating various kinds of request.
     */
    private final RequestFactory factory = new RequestFactory();
    /**
     * Accumulates all summaries.
     */
    private String accumulatedSummary = "";

    /**
     * Tracks last update to accumulated summary.
     * Ensures summaries are processed sequentially.
     */
    private CompletableFuture<Void> lastSummaryUpdate =
            CompletableFuture.completedFuture(null);

    /**
     * Tracks last Q&A operation.
     * Ensures questions run sequentially.
     */
    private CompletableFuture<Void> lastQaUpdate =
            CompletableFuture.completedFuture(null);


    /**
     * Interprets an uploaded image and generates a textual description.
     *
     * @param file uploaded image file (from client)
     * @return textual description of the image
     */
    public CompletableFuture<String> describe(final String file) {
        LOG.info("Received image describe request for file: " + file);

        try {
            // Pass file path to your existing data class
            final WhiteBoardData data = new WhiteBoardData(file);
            LOG.debug("Created WhiteBoardData for file " + file);

            final AiRequestable interpreterRequest = factory.getRequest("DESC", data);
            LOG.debug("Built AI request for image interpretation (DESC)");

            LOG.info("Submitting image interpretation request to AI executor");
            return ASYNC_AI_EXECUTOR.execute(interpreterRequest);
        } catch (IOException e) {
            LOG.error("Failed to execute image describe() for file: " + file, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Regularises a set of input points to produce a smoother output.
     *
     * @param points JSON string containing the points data
     * @return regularised point data as a response
     */
    public  CompletableFuture<String> regularise(final String points) {
        LOG.info("Received regularisation request");

        try {
            LOG.debug("Regularisation payload: received");

            final AiRequestable regulariserRequest = factory.getRequest("REG", points);
            LOG.debug("Built request for regularisation (REG)");

            LOG.info("Submitting regularisation request to AI executor");
            return ASYNC_AI_EXECUTOR.execute(regulariserRequest);
        } catch (Exception e) {
            LOG.error("Regularisation failed", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * API for sentiment analysis.
     * Recieves chats as a json file, does sentiment analysis,
     * and generates insights graph
     * @param chatData JSON object containing the chat data
     * @return a list float values to plot in the sentiment graph.
     */
    public  CompletableFuture<String> sentiment(
            final JsonNode chatData) {
        LOG.info("Received sentiment analysis request");

        try {
            LOG.debug("Chat data received for sentiment analysis");

            AiRequestable sentimentRequest = null;

            sentimentRequest = factory.getRequest("INS", chatData);
            LOG.debug("Built AI request for sentiment analysis (INS)");

            LOG.info("Submitting sentiment analysis request to AI executor");
            return ASYNC_AI_EXECUTOR.execute(sentimentRequest);
        } catch (IOException e) {
            LOG.error("Sentiment analysis failed", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Summarises chat content asynchronously.
     *
     * @param jsonContent chat json
     * @return updated accumulated summary
     */
    public CompletableFuture<String> summariseText(
            final String jsonContent) {
        LOG.info("Received request: summariseText()");

        try {

            lastSummaryUpdate =
                    lastSummaryUpdate.thenCompose(v -> {
                        LOG.info("Preparing content for summarisation");

                        // Prepare content for summarization
                        final String contentToSummarise;

                        if (accumulatedSummary == null || accumulatedSummary.isEmpty()) {
                            contentToSummarise = jsonContent;
                        } else {
                            contentToSummarise = "Previous Summary: " + accumulatedSummary
                                    + "\n\nNew Chat Data: " + jsonContent;
                        }
                        AiRequestable requestSummarise = null;
                        try {
                            requestSummarise = factory.getRequest(
                                    "SUM", contentToSummarise);
                        } catch (IOException e) {
                            LOG.error("Failed to build summarisation request", e);
                            throw new RuntimeException(e);
                        }
                        LOG.info("Dispatching summarisation request");
                        final CompletableFuture<String> future =
                                ASYNC_AI_EXECUTOR.execute(requestSummarise);

                        return future.thenApply(response -> {
                            LOG.info("Summary updated successfully");
                            accumulatedSummary = response;
                            return null;
                        });
                    });

            return lastSummaryUpdate.thenApply(v ->
                    accumulatedSummary);

        } catch (Exception e) {
            LOG.error("Unexpected error in summariseText()", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Clears accumulated summaries.
     *
     * @return success message
     */
    public CompletableFuture<String> clearSummary() {
        LOG.info("Clearing accumulated summary");
        accumulatedSummary = "";
        return CompletableFuture.completedFuture(
                "Summary cleared successfully"
        );
    }



    /**
     * Answers a question using accumulated summary.
     *
     * @param question user question
     * @return AI response
     */
    public CompletableFuture<String> answerQuestion(
            final String question) {
        LOG.info("Received request: answerQuestion()");
        LOG.info("Question received: " + question);

        try {

            final CompletableFuture<String> qaFuture =
                    lastSummaryUpdate.thenCompose(v ->
                            lastQaUpdate.thenCompose(v2 -> {
                                AiRequestable req = null;
                                try {
                                    final String accSum;
                                    if (accumulatedSummary != null) {
                                        accSum = accumulatedSummary;
                                    } else {
                                        accSum = null;
                                    }
                                    req = factory.getRequest("QNA",
                                            question, accumulatedSummary);
                                } catch (IOException e) {
                                    LOG.error("Failed to build Q&A request", e);
                                    throw new RuntimeException(e);
                                }

                                LOG.info("Dispatching Q&A request to executor");
                                return ASYNC_AI_EXECUTOR.execute(
                                        req);
                            })
                    );

            return qaFuture;

        } catch (Exception e) {
            LOG.error("Unexpected error in answerQuestion()", e);
            throw new RuntimeException(
                    "Error processing Q&A request", e);
        }
    }

    /**
     * Creates action items.
     * @param chatData user chatData
     * @return AI response
     */
    public CompletableFuture<String> action(
            final JsonNode chatData) {
        LOG.info("Received action-item generation request");

        try {
            LOG.debug("Action raw chat input received");

            final AiRequestable actionRequest = factory.getRequest("ACTION", chatData);
            LOG.debug("Built AI request for ACTION");

            LOG.info("Submitting ACTION request to AI executor");
            return ASYNC_AI_EXECUTOR.execute(actionRequest);
        } catch (IOException e) {
            LOG.error("Failed to generate action items", e);
            throw new RuntimeException(e);
        }
    }
}