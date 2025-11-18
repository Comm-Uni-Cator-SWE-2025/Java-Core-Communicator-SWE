/**
 * Author Berelli Gouthami
 */


package com.swe.aiinsights.response;

/**
 * Represents the AI's response to a chat summarisation request.
 * <p>
 * This class stores the summarised text returned by the model and
 * identifies the response type for downstream handling.
 * </p>
 */
public class SummariserResponse implements AiResponse {

    /**
     * The summary text returned by the AI model.
     */
    private String responseText;

    /**
     * Sets the summarised response text.
     *
     * @param text the text returned by the AI
     */
    @Override
    public void setResponse(final String text) {
        this.responseText = text;
    }

    /**
     * Returns the summarised response text.
     *
     * @return the AI-generated summary
     */
    @Override
    public String getResponse() {
        return this.responseText;
    }

    /**
     * Provides direct access to the summary text.
     * <p>
     * This is a helper method for readability
     * </p>
     *
     * @return the AI-generated summary
     */
    public String getResponseText() {
        return responseText;
    }
}
