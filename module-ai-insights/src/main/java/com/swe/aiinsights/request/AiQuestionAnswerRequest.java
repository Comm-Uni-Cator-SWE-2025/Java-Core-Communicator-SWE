package com.swe.aiinsights.request;

import java.util.HashMap;
import java.util.Map;

/**
 * A request object for Question & Answer operations.
 * Contains both the user's question and the accumulated summary.
 */
public class AiQuestionAnswerRequest implements AiRequestable<String> {

    /**
     * Stores metadata including question, summary and prompt.
     */
    private Map<String, String> metaData;

    /**
     * Stores the type of request. For Q&A, it is "QNA".
     */
    private String type;

    /**
     * Constructs a Q&A request with question and accumulated summary.
     *
     * @param question the user question
     * @param accumulatedSummary the accumulated chat summary
     */
    public AiQuestionAnswerRequest(
            final String question,
            final String accumulatedSummary) {

        this.metaData = new HashMap<>();
        this.metaData.put("Question", question);
        this.metaData.put("Summary", accumulatedSummary);

        final String prompt =
                "You are an intelligent Q&A system.\n"
                        + "You will receive two things:\n"
                        + "1. ACCUMULATED_SUMMARY – this contains "
                        + "collected information.\n"
                        + "2. USER_QUESTION – the question asked by the user.\n\n"
                        + "Your task:\n"
                        + "- First decide if the USER_QUESTION requires "
                        + "information from the ACCUMULATED_SUMMARY.\n"
                        + "(Ignore this decision in your final answer — "
                        + "do NOT output classification.)\n"
                        + "- If the answer can be found in the "
                        + "ACCUMULATED_SUMMARY, use only that.\n"
                        + "- If the summary does not contain the answer, "
                        + "reply exactly:\n"
                        + "\"The information is missing from the context.\"\n"
                        + "- If the question does NOT require the summary, "
                        + "answer normally using general knowledge.\n\n"
                        + "ACCUMULATED_SUMMARY:\n"
                        + accumulatedSummary + "\n\n"
                        + "USER_QUESTION:\n"
                        + question;

        this.metaData.put("RequestPrompt", prompt);
        this.type = "QNA";
    }

    /**
     * Gets the prompt sent to the AI.
     *
     * @return prompt text
     */
    @Override
    public String getContext() {
        return metaData.get("RequestPrompt");
    }

    /**
     * Returns only the user question.
     *
     * @return the question
     */
    @Override
    public String getInput() {
        return metaData.get("Question");
    }

    /**
     * Returns the request type "QNA".
     *
     * @return request type
     */
    @Override
    public String getReqType() {
        return type;
    }
}
