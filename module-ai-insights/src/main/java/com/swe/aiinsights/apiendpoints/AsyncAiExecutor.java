/**
 * Author : Abhirami R Iyer
 */
package com.swe.aiinsights.apiendpoints;

import com.swe.aiinsights.aiservice.GeminiService;
import com.swe.aiinsights.aiservice.LlmService;
import com.swe.aiinsights.aiservice.OllamaService;
import com.swe.aiinsights.configu.AsyncConfig;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.response.AiResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class AsyncAiExecutor {


    private LlmService llmService = new GeminiService();


    private Executor aiExecutor = new AsyncConfig().aiExecutor();


    public CompletableFuture<String> execute(AiRequestable req) {

        return CompletableFuture.supplyAsync(() -> {
                    try {
                        AiResponse aiResponse = llmService.runProcess(req);
                        return aiResponse.getResponse();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }, aiExecutor);
    }

}
