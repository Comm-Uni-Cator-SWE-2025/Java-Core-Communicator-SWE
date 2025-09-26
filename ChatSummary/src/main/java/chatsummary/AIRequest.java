package chatsummary;

/**
 * Contains instructions for what type of summary to generate.
 */
public class AIRequest {
    /** Type of summary requested (e.g., "SUMMARY", "BULLET_SUMMARY"). */
    private String requestType;
    /** The actual text prompt to send to AI. */
    private String prompt;
    /** Additional data like message limits. */
    private Object metaData;

    /**
     * Constructor for AIRequest with type and prompt.
     *
     * @param requestTypeName the type of request
     * @param promptText the prompt text
     */
    public AIRequest(final String requestTypeName, final String promptText) {
        this.requestType = requestTypeName;
        this.prompt = promptText;
    }

    /**
     * Constructor for AIRequest with type, prompt, and metadata.
     *
     * @param requestTypeName the type of request
     * @param promptText the prompt text
     * @param metaDataObj additional metadata
     */
    public AIRequest(final String requestTypeName, final String promptText, final Object metaDataObj) {
        this.requestType = requestTypeName;
        this.prompt = promptText;
        this.metaData = metaDataObj;
    }

    /**
     * Gets the request type.
     *
     * @return the request type
     */
    public String getRequestType() {
        return requestType;
    }

    /**
     * Gets the prompt.
     *
     * @return the prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public Object getMetaData() {
        return metaData;
    }

    /**
     * Sets the request type.
     *
     * @param requestTypeName the request type to set
     */
    public void setRequestType(final String requestTypeName) {
        this.requestType = requestTypeName;
    }

    /**
     * Sets the prompt.
     *
     * @param promptText the prompt to set
     */
    public void setPrompt(final String promptText) {
        this.prompt = promptText;
    }

    /**
     * Sets the metadata.
     *
     * @param metaDataObj the metadata to set
     */
    public void setMetaData(final Object metaDataObj) {
        this.metaData = metaDataObj;
    }

    @Override
    public String toString() {
        return "AIRequest{"
                + "requestType='" + requestType + '\''
                + ", prompt='" + prompt + '\''
                + ", metaData=" + metaData
                + '}';
    }
}