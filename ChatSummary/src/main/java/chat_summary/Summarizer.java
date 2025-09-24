package chat_summary;

public class Summarizer implements ISummarizer {
    private GeminiLLMService geminiService;

    public Summarizer() {
        this.geminiService = new GeminiLLMService();
    }

    public Summarizer(GeminiLLMService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public String generateSummary(IMeetingData meetingData, AIRequest request) {
        String requestType = request.getRequestType();

        switch (requestType) {
            case "SUMMARY":
                return generateParagraphSummary(meetingData);
            case "SUMMARY_LIMITED":
                Integer maxMessages = (Integer) request.getMetaData();
                return generateParagraphSummary(meetingData, maxMessages != null ? maxMessages : 10);
            case "BULLET_SUMMARY":
                return generateBulletSummary(meetingData);
            case "BULLET_SUMMARY_LIMITED":
                Integer maxBulletMessages = (Integer) request.getMetaData();
                return generateBulletSummary(meetingData, maxBulletMessages != null ? maxBulletMessages : 10);
            default:
                return "Unsupported request type: " + requestType;
        }
    }

    private String generateParagraphSummary(IMeetingData meetingData) {
        String prompt = createParagraphSummaryPrompt(meetingData.getChatHistory());
        return geminiService.generateContent(prompt);
    }

    private String generateParagraphSummary(IMeetingData meetingData, int maxMessages) {
        String prompt = createParagraphSummaryPrompt(meetingData.getChatHistory(maxMessages));
        return geminiService.generateContent(prompt);
    }

    private String generateBulletSummary(IMeetingData meetingData) {
        String prompt = createBulletSummaryPrompt(meetingData.getChatHistory());
        return geminiService.generateContent(prompt);
    }

    private String generateBulletSummary(IMeetingData meetingData, int maxMessages) {
        String prompt = createBulletSummaryPrompt(meetingData.getChatHistory(maxMessages));
        return geminiService.generateContent(prompt);
    }

    private String createParagraphSummaryPrompt(String chatHistory) {
        return String.format(
                "Based on the following meeting discussion, create a natural paragraph summary " +
                        "that captures the flow of conversation and key points discussed.\n\n" +
                        "Write it in a way that shows who said what and how the conversation progressed. " +
                        "Make it sound like a natural narrative of the meeting.\n\n" +
                        "Meeting discussion:\n%s\n\n" +
                        "Please provide a paragraph-style summary:",
                chatHistory
        );
    }

    private String createBulletSummaryPrompt(String chatHistory) {
        return String.format(
                "Based on the following meeting discussion, create a concise summary with bullet points. " +
                        "Focus on the main discussion points, decisions made, and key takeaways.\n\n" +
                        "Meeting discussion:\n%s\n\n" +
                        "Please provide a bullet-point summary:",
                chatHistory
        );
    }
}
