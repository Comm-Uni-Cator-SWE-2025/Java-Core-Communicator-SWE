package com.swe.aiinsights.apiendpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.aiinsights.request.AiQuestionAnswerRequest;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.questionanswergenerator.QuestionAnswerGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionAnswerTest {

    private AiClientService client;
    private StubExecutor stub;

    // ---- Stub executor ---------------------------------

    private static class StubExecutor extends AsyncAiExecutor {
        private String nextResponse;

        public void setResponse(String r) {
            nextResponse = r;
        }

        @Override
        public CompletableFuture<String> execute(AiRequestable req) {
            return CompletableFuture.completedFuture(nextResponse);
        }
    }

    // ---- Spy client that uses stub instead of real executor ----
    private class SpyClient extends AiClientService {
        @Override
        public CompletableFuture<String> answerQuestion(String q) {
            return stub.execute(null);
        }

        @Override
        public CompletableFuture<String> summariseText(String txt) {
            return stub.execute(null)
                    .thenApply(summary -> {
                        try {
                            var f = AiClientService.class.getDeclaredField("accumulatedSummary");
                            f.setAccessible(true);
                            f.set(this, summary);
                        } catch (Exception e) {}
                        return summary;
                    });
        }
    }

    @BeforeEach
    void setup() {
        stub = new StubExecutor();
        client = new SpyClient();   // use spy instead of reflection
    }

    // ----------------------------------------------------------------------
    // TESTS
    // ----------------------------------------------------------------------

    @Test
    void testGenericQuestion() throws Exception {
        stub.setResponse("GENERIC");
        String result = client.answerQuestion("What?").get();
        assertEquals("GENERIC", result);
    }

    @Test
    void testContextualQuestion() throws Exception {
        stub.setResponse("SUM1");
        client.summariseText("chat").get();

        stub.setResponse("CTX");
        String r = client.answerQuestion("context?").get();

        assertEquals("CTX", r);
    }

    @Test
    void testEmptySummary() throws Exception {
        stub.setResponse("");
        String r = client.answerQuestion("hello").get();
        assertEquals("", r);
    }

    @Test
    void testQnaAfterClearingSummary() throws Exception {
        stub.setResponse("AAA");
        client.summariseText("X").get();

        client.clearSummary().get();

        stub.setResponse("NOCTX");
        String r = client.answerQuestion("q").get();

        assertEquals("NOCTX", r);
    }

    @Test
    void testGeneratorJsonStructure() throws Exception {
        QuestionAnswerGenerator gen = new QuestionAnswerGenerator();

        AiQuestionAnswerRequest req =
                new AiQuestionAnswerRequest("What?", "you said hi");

        String json = gen.processRequest(new ObjectMapper(), req);

        assertTrue(json.contains("What?"));
        assertTrue(json.contains("hi"));
    }

    @Test
    void testRequestObjectFields() {
        AiQuestionAnswerRequest req =
                new AiQuestionAnswerRequest("Why?", "sky blue");

        assertEquals("Why?", req.getInput());
        assertTrue(req.getContext().contains("sky blue"));
        assertEquals("QNA", req.getReqType());
    }
}
