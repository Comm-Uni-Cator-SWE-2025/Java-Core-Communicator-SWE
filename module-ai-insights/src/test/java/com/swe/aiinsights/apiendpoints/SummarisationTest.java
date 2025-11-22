package com.swe.aiinsights.apiendpoints;

import com.swe.aiinsights.request.AiRequestable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

import sun.misc.Unsafe;

public class SummarisationTest {

    private AiClientService client;
    private StubExecutor stub;

    /**
     * Stub executor that returns fixed string responses.
     */
    private static class StubExecutor extends AsyncAiExecutor {
        private String nextResponse;
        private String lastRequestInput;

        public void setResponse(String r) {
            this.nextResponse = r;
        }

        public String getLastRequestInput() {
            return lastRequestInput;
        }

        @Override
        public CompletableFuture<String> execute(AiRequestable req) {
            // Capture the input to verify it contains previous summary
            Object input = req.getInput();
            this.lastRequestInput = (input != null) ? input.toString() : "";
            return CompletableFuture.completedFuture(nextResponse);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        client = new AiClientService();
        stub = new StubExecutor();

        // Get the final static field
        Field f = AiClientService.class.getDeclaredField("asyncExecutor");
        f.setAccessible(true);

        // Get Unsafe
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);

        // Static fields belong to the class, not an instance
        Object base = unsafe.staticFieldBase(f);
        long offset = unsafe.staticFieldOffset(f);

        // Replace the static final executor with our stub
        unsafe.putObject(base, offset, stub);
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

        // Verify first call doesn't include "Previous Summary:"
        assertFalse(stub.getLastRequestInput().contains("Previous Summary:"));
    }

    // -----------------------------------------------------------------------------------------
    // 2. Multiple summaries - new logic: re-summarizes with previous context
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

        // NEW LOGIC: Second summary replaces (it already includes previous context)
        assertEquals("FILE_SUM2", f.get(client));

        // Verify second call includes previous summary in input
        assertTrue(stub.getLastRequestInput().contains("Previous Summary:"));
        assertTrue(stub.getLastRequestInput().contains("FILE_SUM1"));
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
    // 7. Sequential chaining - new logic
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

        // NEW LOGIC: S2 is the re-summarized version containing S1+new chat
        assertEquals("S2", f.get(client));
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

    // -----------------------------------------------------------------------------------------
    // 9. Three summaries in sequence - verify context propagation
    // -----------------------------------------------------------------------------------------
    @Test
    void testThreeSummariesInSequence() throws Exception {
        String json = loadChatJson();

        stub.setResponse("SUMMARY_1");
        client.summariseText(json).get();

        stub.setResponse("SUMMARY_2");
        client.summariseText(json).get();

        stub.setResponse("SUMMARY_3");
        String result = client.summariseText(json).get();

        Field f = AiClientService.class.getDeclaredField("accumulatedSummary");
        f.setAccessible(true);

        assertEquals("SUMMARY_3", f.get(client));
        assertEquals("SUMMARY_3", result);

        // Verify the last input contained SUMMARY_2
        assertTrue(stub.getLastRequestInput().contains("SUMMARY_2"));
    }

    // -----------------------------------------------------------------------------------------
    // 10. Clear and restart accumulation
    // -----------------------------------------------------------------------------------------
    @Test
    void testClearAndRestart() throws Exception {
        String json = loadChatJson();

        stub.setResponse("BEFORE_CLEAR");
        client.summariseText(json).get();

        client.clearSummary().get();

        stub.setResponse("AFTER_CLEAR");
        client.summariseText(json).get();

        Field f = AiClientService.class.getDeclaredField("accumulatedSummary");
        f.setAccessible(true);

        assertEquals("AFTER_CLEAR", f.get(client));

        // After clear, should not contain previous summary
        assertFalse(stub.getLastRequestInput().contains("Previous Summary:"));
        assertFalse(stub.getLastRequestInput().contains("BEFORE_CLEAR"));
    }

    // -----------------------------------------------------------------------------------------
    // 11. Q&A after multiple summaries
    // -----------------------------------------------------------------------------------------
    @Test
    void testQnaAfterMultipleSummaries() throws Exception {
        String json = loadChatJson();

        stub.setResponse("SUM1");
        client.summariseText(json).get();

        stub.setResponse("SUM2");
        client.summariseText(json).get();

        stub.setResponse("SUM3");
        client.summariseText(json).get();

        stub.setResponse("FINAL_ANSWER");
        String answer = client.answerQuestion("What happened?").get();

        assertEquals("FINAL_ANSWER", answer);
    }

    // -----------------------------------------------------------------------------------------
    // 12. Verify input format on second summary
    // -----------------------------------------------------------------------------------------
    @Test
    void testInputFormatOnSecondSummary() throws Exception {
        String json = loadChatJson();

        stub.setResponse("FIRST");
        client.summariseText(json).get();

        stub.setResponse("SECOND");
        client.summariseText(json).get();

        String lastInput = stub.getLastRequestInput();

        // Verify format: "Previous Summary: FIRST\n\nNew Chat Data: {json}"
        assertTrue(lastInput.contains("Previous Summary: FIRST"));
        assertTrue(lastInput.contains("New Chat Data:"));
        assertTrue(lastInput.contains(json));
    }

    // -----------------------------------------------------------------------------------------
    // 13. Multiple Q&A calls with same summary
    // -----------------------------------------------------------------------------------------
    @Test
    void testMultipleQnaWithSameSummary() throws Exception {
        String json = loadChatJson();

        stub.setResponse("ACCUMULATED");
        client.summariseText(json).get();

        stub.setResponse("ANSWER1");
        String ans1 = client.answerQuestion("Q1").get();

        stub.setResponse("ANSWER2");
        String ans2 = client.answerQuestion("Q2").get();

        assertEquals("ANSWER1", ans1);
        assertEquals("ANSWER2", ans2);
    }

    // -----------------------------------------------------------------------------------------
    // 14. Edge case: null summary becomes empty string
    // -----------------------------------------------------------------------------------------
    @Test
    void testNullSummaryHandling() throws Exception {
        Field f = AiClientService.class.getDeclaredField("accumulatedSummary");
        f.setAccessible(true);
        f.set(client, null);

        String json = loadChatJson();
        stub.setResponse("NEW_SUMMARY");
        client.summariseText(json).get();

        assertEquals("NEW_SUMMARY", f.get(client));

        // Should not contain "Previous Summary:" when starting from null
        assertFalse(stub.getLastRequestInput().contains("Previous Summary:"));
    }
}