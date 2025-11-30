/*
 * -----------------------------------------------------------------------------
 *  File: ResponseClassesTest.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201003
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */


package com.swe.aiinsights;

import com.swe.aiinsights.response.ActionItemsResponse;
import com.swe.aiinsights.response.AiResponse;
import com.swe.aiinsights.response.InsightsResponse;
import com.swe.aiinsights.response.InterpreterResponse;
import com.swe.aiinsights.response.QuestionAnswerResponse;
import com.swe.aiinsights.response.RegulariserResponse;
import com.swe.aiinsights.response.SummariserResponse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Complete test suite for all Response.
 */
public class ResponseClassesTest {

    /**
     * Generic validator for response classes implementing basic
     * getResponse and setResponse behavior.
     * @param response response to test
     */
    private void verifyBasicResponse(final AiResponse response) {
        assertNull(response.getResponse());

        response.setResponse("X");
        assertEquals("X", response.getResponse());

        response.setResponse(null);
        assertNull(response.getResponse());

        response.setResponse("");
        assertEquals("", response.getResponse());

        response.setResponse("A");
        response.setResponse("B");
        assertEquals("B", response.getResponse());
    }

    /**
     * Tests RegulariserResponse behavior.
     */
    @Test
    void testRegulariserResponse() {
        verifyBasicResponse(new RegulariserResponse());
    }

    /**
     * Tests QuestionAnswerResponse behavior.
     */
    @Test
    void testQuestionAnswerResponse() {
        verifyBasicResponse(new QuestionAnswerResponse());
    }

    /**
     * Tests InsightsResponse behavior.
     */
    @Test
    void testInsightsResponse() {
        verifyBasicResponse(new InsightsResponse());
    }

    /**
     * Tests InterpreterResponse behavior.
     */
    @Test
    void testInterpreterResponse() {
        verifyBasicResponse(new InterpreterResponse());
    }

    /**
     * Tests ActionItemsResponse behavior.
     */
    @Test
    void testActionItemsResponse() {
        verifyBasicResponse(new ActionItemsResponse());
    }

    /**
     * Tests SummariserResponse behavior.
     */
    @Test
    void testSummariserResponse() {
        final SummariserResponse response = new SummariserResponse();

        verifyBasicResponse(response);

        response.setResponse("Test");
        assertEquals("Test", response.getResponseText());

        response.setResponse(null);
        assertNull(response.getResponseText());

        response.setResponse("");
        assertEquals("", response.getResponseText());
    }
}