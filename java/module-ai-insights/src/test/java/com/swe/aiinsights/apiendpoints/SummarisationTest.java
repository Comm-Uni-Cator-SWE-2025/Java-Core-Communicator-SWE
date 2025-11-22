package com.swe.aiinsights.apiendpoints;

import com.swe.aiinsights.request.AiRequestable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class SummarisationTest {

    private AiClientService client;
    private StubExecutor stub;

    /**
     * Stub executor that returns fixed string responses.
     */
    private static class StubExecutor extends AsyncAiExecutor {
        private String nextResponse;

        public void setResponse(String r) {
            this.nextResponse = r;
        }

        @Override
        public CompletableFuture<String> execute(AiRequestable req) {
            return CompletableFuture.completedFuture(nextResponse);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        client = new AiClientService();

        stub = new StubExecutor();

        Field f = AiClientService.class.getDeclaredField("asyncExecutor");
        f.setAccessible(true);
        f.set(client, stub);
    }

    // Utility to load test json
    private String loadChatJson() throws Exception {
        return Files.readString(
                Paths.get("src/test/resources/chatData/chat_data.json")
        );
    }

    // -----------------------------------------------------------------------------------------
    // 1. First Summary using file input
    // -----------------------------------------------------------------------------------------
    @Test
    void testSingleSummary() throws Exception {
        stub.setResponse("SUM_FROM_FILE");

        String json = loadChatJson();

        String result = client.summariseText(json).get();

        assertEquals("SUM_FROM_FILE", result);
    }

    // -----------------------------------------------------------------------------------------
    // 2. Multiple summaries accumulate using "+"
    // -----------------------------------------------------------------------------------------
    @Test
    void testAccumulatedSummaries() throws Exception {
        String json = loadChatJson();

        stub.setResponse("FILE_SUM1");
        client.summariseText(json).get();

        stub.setResponse("FILE_SUM2");
        client.summariseText(json).get();

        Field f = AiClientService.class.getDeclaredField("accumulatedSummary");
        f.setAccessible(true);

        assertEquals("FILE_SUM1+FILE_SUM2", f.get(client));
    }

    // -----------------------------------------------------------------------------------------
    // 3. Empty summary
    // -----------------------------------------------------------------------------------------
    @Test
    void testEmptySummary() throws Exception {
        String json = loadChatJson();

        stub.setResponse("");
        client.summariseText(json).get();

        Field f = AiClientService.class.getDeclaredField("accumulatedSummary");
        f.setAccessible(true);

        assertEquals("", f.get(client));
    }

    // -----------------------------------------------------------------------------------------
    // 4. Clear summary
    // -----------------------------------------------------------------------------------------
    @Test
    void testClearSummary() throws Exception {
        String json = loadChatJson();

        stub.setResponse("AAA");
        client.summariseText(json).get();

        client.clearSummary().get();

        Field f = AiClientService.class.getDeclaredField("accumulatedSummary");
        f.setAccessible(true);

        assertEquals("", f.get(client));
    }

    // -----------------------------------------------------------------------------------------
    // 5. Q&A should use accumulated summary
    // -----------------------------------------------------------------------------------------
    @Test
    void testQnaUsingSummary() throws Exception {
        String json = loadChatJson();

        stub.setResponse("FILE_SUM");
        client.summariseText(json).get();

        stub.setResponse("ANSWER_FROM_SUMMARY");
        String result = client.answerQuestion("what?").get();

        assertEquals("ANSWER_FROM_SUMMARY", result);
    }

    // -----------------------------------------------------------------------------------------
    // 6. Q&A when no summary
    // -----------------------------------------------------------------------------------------
    @Test
    void testQnaNoSummary() throws Exception {
        stub.setResponse("GENERIC");
        String r = client.answerQuestion("hello").get();

        assertEquals("GENERIC", r);
    }

    // -----------------------------------------------------------------------------------------
    // 7. Sequential chaining
    // -----------------------------------------------------------------------------------------
    @Test
    void testSequentialChaining() throws Exception {
        String json = loadChatJson();

        stub.setResponse("S1");
        client.summariseText(json).get();

        stub.setResponse("S2");
        client.summariseText(json).get();

        stub.setResponse("Q-ANS");
        String result = client.answerQuestion("q").get();

        assertEquals("Q-ANS", result);

        Field f = AiClientService.class.getDeclaredField("accumulatedSummary");
        f.setAccessible(true);

        assertEquals("S1+S2", f.get(client));
    }

    // -----------------------------------------------------------------------------------------
    // 8. Null safeguard
    // -----------------------------------------------------------------------------------------
    @Test
    void testNullSummarySafety() throws Exception {
        Field f = AiClientService.class.getDeclaredField("accumulatedSummary");
        f.setAccessible(true);
        f.set(client, null);

        stub.setResponse("NULL_SAFE");
        String r = client.answerQuestion("test").get();

        assertEquals("NULL_SAFE", r);
    }
}

