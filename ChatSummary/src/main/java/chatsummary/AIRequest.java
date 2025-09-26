package chatsummary;

/**
 * Contains instructions for what type of summary to generate.
 */
public class AIRequest {
    private String requestType;
    private String prompt;
    private Object metaData;

    //constructor
    public AIRequest(final String requestTypeName, final String promptText) {
        this.requestType = requestTypeName;
        this.prompt = promptText;
    }


    public AIRequest(final String requestTypeName, final String promptText, final Object metaDataObj) {
        this.requestType = requestTypeName;
        this.prompt = promptText;
        this.metaData = metaDataObj;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getPrompt() {
        return prompt;
    }

    public Object getMetaData() {
        return metaData;
    }


    public void setRequestType(final String requestTypeName) {
        this.requestType = requestTypeName;
    }


    public void setPrompt(final String promptText) {
        this.prompt = promptText;
    }

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