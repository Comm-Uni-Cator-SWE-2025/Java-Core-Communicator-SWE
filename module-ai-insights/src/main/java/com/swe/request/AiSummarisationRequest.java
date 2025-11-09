
/**
 * Author Berelli Gouthami
 */
package request;

/**
 * Creates a new summarisation request.
 */
public class AiSummarisationRequest implements AiRequestable<String> {

    /**
     * Raw chat data in JSON format.
     */
    private final String jsonInput;

    /**
     * Creates a summarisation request.
     *
     * @param inputJson the chat data JSON
     */
    public AiSummarisationRequest(final String inputJson) {
        this.jsonInput = inputJson;
    }

    /**
     * Provides a general instruction for the AI model.
     *
     * @return summarisation context prompt
     */
    @Override
    public String getContext() {
        return "Summarize the "
               + "following chat data into a concise, meaningful summary.";
    }

    /**
     * Returns the chat input to be summarised.
     *
     * @return JSON-formatted chat string
     */
    @Override
    public String getInput() {
        return jsonInput;
    }

    /**
     * Specifies the type of AI request being made.
     *
     * @return request type identifier
     */
    @Override
    public String getReqType() {
        return "SUMMARISE";
    }
}
