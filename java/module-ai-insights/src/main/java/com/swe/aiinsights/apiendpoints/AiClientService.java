/**
 * Authors : Abhirami R Iyer
 */
package com.swe.aiinsights.apiendpoints;


import com.fasterxml.jackson.databind.JsonNode;
import com.swe.aiinsights.data.WhiteBoardData;
import com.swe.aiinsights.request.AiDescriptionRequest;
import com.swe.aiinsights.request.AiRegularisationRequest;
import com.swe.aiinsights.request.AiInsightsRequest;
import com.swe.aiinsights.request.AiSummarisationRequest;
import com.swe.aiinsights.request.AiActionItemsRequest;
import java.util.concurrent.CompletableFuture;
import com.swe.aiinsights.request.AiQuestionAnswerRequest;



import java.io.IOException;
import java.nio.file.Path;

/**
 * AI client service.
 * contains the api endpoints for all the services offered by the AI module.
 */

public class AiClientService {

    /**
     * Executor used to run async AI calls.
     */
    private static final AsyncAiExecutor asyncExecutor = new AsyncAiExecutor();
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
    public CompletableFuture<String> describe(final Path file) {
        try {


            // Pass file path to your existing data class
            WhiteBoardData data = new WhiteBoardData(file.toString());
            return asyncExecutor.execute(new AiDescriptionRequest(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Regularises a set of input points to produce a smoother output.
     *
     * @param points JSON string containing the points data
     * @return regularised point data as a response
     */
    public  CompletableFuture<String> regularise (final String points) {
        try {
            return asyncExecutor.execute(new AiRegularisationRequest(points));
        } catch (Exception e) {
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
            final JsonNode chatData){
        try {
            return asyncExecutor.execute(new AiInsightsRequest(chatData));
        } catch (IOException e) {
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

        try {
            CompletableFuture<String> future =
                    asyncExecutor.execute(
                            new AiSummarisationRequest(
                                    jsonContent));

            lastSummaryUpdate =
                    lastSummaryUpdate.thenCompose(v ->
                            future.thenApply(response -> {

                                if (accumulatedSummary == null
                                        || accumulatedSummary
                                        .isEmpty()) {
                                    accumulatedSummary =
                                            response;
                                } else {
                                    accumulatedSummary =
                                            accumulatedSummary
                                                    + "+"
                                                    + response;
                                }

                                return null;
                            })
                    );

            return lastSummaryUpdate.thenApply(v ->
                    accumulatedSummary);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Clears accumulated summaries.
     *
     * @return success message
     */
    public CompletableFuture<String> clearSummary() {
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

        try {
            CompletableFuture<String> qaFuture =
                    lastSummaryUpdate.thenCompose(v ->
                            lastQaUpdate.thenCompose(v2 -> {

                                AiQuestionAnswerRequest req =
                                        new AiQuestionAnswerRequest(
                                                question,
                                                accumulatedSummary
                                                        != null
                                                        ? accumulatedSummary
                                                        : ""
                                        );

                                return asyncExecutor.execute(
                                        req);
                            })
                    );

            return qaFuture;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error processing Q&A request", e);
        }
    }


    public CompletableFuture<String> action(
        final JsonNode chatData){
        try {
            return asyncExecutor.execute(new AiActionItemsRequest(chatData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
