package chat_summary;



public class AIRequest {
    private String requestType;
    private String prompt;
    private Object metaData;

    public AIRequest(String requestType, String prompt) {
        this.requestType = requestType;
        this.prompt = prompt;
    }

    public AIRequest(String requestType, String prompt, Object metaData) {
        this.requestType = requestType;
        this.prompt = prompt;
        this.metaData = metaData;
    }

    // Getters
    public String getRequestType() { return requestType; }
    public String getPrompt() { return prompt; }
    public Object getMetaData() { return metaData; }

    // Setters
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setMetaData(Object metaData) { this.metaData = metaData; }

    @Override
    public String toString() {
        return "AIRequest{" +
                "requestType='" + requestType + '\'' +
                ", prompt='" + prompt + '\'' +
                ", metaData=" + metaData +
                '}';
    }
}