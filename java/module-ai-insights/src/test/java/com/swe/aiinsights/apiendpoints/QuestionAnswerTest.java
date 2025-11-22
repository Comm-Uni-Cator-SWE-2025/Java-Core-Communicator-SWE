package com.swe.aiinsights.apiendpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.aiinsights.request.AiQuestionAnswerRequest;
import com.swe.aiinsights.questionanswergenerator.QuestionAnswerGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests only the Q&A pipeline of AiClientService.
 * Uses a StubExecutor instead of mocking AsyncAiExecutor.
 */
public class QuestionAnswerTest {

    private AiClientService client;

    /**
     * Stub executor used to return custom responses.
     */
    private static class StubExecutor extends AsyncAiExecutor {
        private String nextResponse;

        public void setResponse(String response) {
            this.nextResponse = response;
        }

        @Override
        public CompletableFuture<String> execute(com.swe.aiinsights.request.AiRequestable req) {
            return CompletableFuture.completedFuture(nextResponse);
        }
    }

    private StubExecutor stub;

    @BeforeEach
    void setup() throws Exception {
        client = new AiClientService();

        // inject stub into client via reflection
        stub = new StubExecutor();
        Field f = AiClientService.class.getDeclaredField("asyncExecutor");
        f.setAccessible(true);
        f.set(client, stub);
    }

    // ----------------------------------------------------------------------
    // 1. Test Q&A WITHOUT summary (generic question)
    // ----------------------------------------------------------------------
    @Test
    void testGenericQuestion() throws Exception {
        stub.setResponse("GENERIC ANSWER");

        String result = client.answerQuestion("Who is the PM of India?").get();

        assertEquals("GENERIC ANSWER", result);
    }

    // ----------------------------------------------------------------------
    // 2. Test Q&A WITH summary (contextual)
    // ----------------------------------------------------------------------
    @Test
    void testContextualQuestion() throws Exception {
        // first create summary
        stub.setResponse("User loves cats");
        client.summariseText("{chat}").get();

        // then ask contextual question
        stub.setResponse("User likes cats a lot");
        String reply = client.answerQuestion("What animals does user like?").get();

        assertEquals("User likes cats a lot", reply);
    }

    // ----------------------------------------------------------------------
    // 3. Test Q&A when summary is empty string
    // ----------------------------------------------------------------------
    @Test
    void testEmptySummary() throws Exception {
        // directly ask question without summary
        stub.setResponse("GENERIC AAA");

        String reply = client.answerQuestion("Hello?").get();

        assertEquals("GENERIC AAA", reply);
    }

    // ----------------------------------------------------------------------
    // 4. Test Q&A after summary was cleared
    // ----------------------------------------------------------------------
    @Test
    void testQnaAfterClearingSummary() throws Exception {
        // build summary
        stub.setResponse("SUM1");
        client.summariseText("A").get();

        // clear summary
        client.clearSummary().get();

        stub.setResponse("ANSWER WITHOUT CONTEXT");
        String reply = client.answerQuestion("Tell me something").get();

        assertEquals("ANSWER WITHOUT CONTEXT", reply);
    }

    // ----------------------------------------------------------------------
    // 5. Test the generator (full coverage)
    // ----------------------------------------------------------------------
    @Test
    void testGeneratorJsonStructure() throws Exception {
        QuestionAnswerGenerator gen = new QuestionAnswerGenerator();

        AiQuestionAnswerRequest req =
                new AiQuestionAnswerRequest(
                        "What is my name?",
                        "You told your name is Gouthami."
                );

        String json = gen.processRequest(new ObjectMapper(), req);

        // Validate structure that DEFINITELY exists
        assertTrue(json.contains("Gouthami"));
        assertTrue(json.contains("What is my name?"));
        assertTrue(json.contains("contents"));
        assertTrue(json.contains("parts"));
    }

    // ----------------------------------------------------------------------
    // 6. Test AiQuestionAnswerRequest metadata getters
    // ----------------------------------------------------------------------
    @Test
    void testRequestObjectFields() {
        AiQuestionAnswerRequest req =
                new AiQuestionAnswerRequest("Why sky blue?", "sky scattering info");

        assertEquals("Why sky blue?", req.getInput());
        assertTrue(req.getContext().contains("sky scattering info"));
        assertEquals("QNA", req.getReqType());
    }
}
