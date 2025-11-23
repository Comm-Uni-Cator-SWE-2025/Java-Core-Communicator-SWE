package com.swe.aiinsights.response;

/**
 * Represents the AI's response to a Question & Answer request.
 */
public class QuestionAnswerResponse implements AiResponse {

    /**
     * Stores the response text returned by the AI.
     */
    private String responseText;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResponse(final String text) {
        this.responseText = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse() {
        return this.responseText;
    }
}
