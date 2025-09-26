package chatsummary;

/**
 * Summarizer implementation that generates different types of summaries.
 */
public class Summarizer implements ISummarizer {

    private ILLMService llmService;

    private static final int DEFAULT_MESSAGE_LIMIT = 10;

    public Summarizer() {
        this.llmService = new GeminiLLMService();
    }


    public Summarizer(final ILLMService llmServiceInstance) {
        this.llmService = llmServiceInstance;
    }

    @Override
    public String generateSummary(final IMeetingData meetingData, final AIRequest request) {
        final String requestType = request.getRequestType();

        switch (requestType) {
            case "SUMMARY":
                return generateParagraphSummary(meetingData);
            case "SUMMARY_LIMITED":
                final Integer maxMessages = (Integer) request.getMetaData();
                final int messageLimit;
                if (maxMessages != null) {
                    messageLimit = maxMessages;
                } else {
                    messageLimit = DEFAULT_MESSAGE_LIMIT;
                }
            case "BULLET_SUMMARY":
                return generateBulletSummary(meetingData);
            case "BULLET_SUMMARY_LIMITED":
                final Integer maxBulletMessages = (Integer) request.getMetaData();
                final int bulletLimit;
                if (maxBulletMessages != null) {
                    bulletLimit = maxBulletMessages;
                } else {
                    bulletLimit = DEFAULT_MESSAGE_LIMIT;
                }
                return generateBulletSummary(meetingData, bulletLimit);
            default:
                return "Unsupported request type: " + requestType;
        }
    }


    private String generateParagraphSummary(final IMeetingData meetingData) {
        final String prompt = createParagraphSummaryPrompt(meetingData.getChatHistory());
        return llmService.generateContent(prompt);
    }

    private String generateParagraphSummary(final IMeetingData meetingData, final int maxMessages) {
        final String prompt = createParagraphSummaryPrompt(meetingData.getChatHistory(maxMessages));
        return llmService.generateContent(prompt);
    }


    private String generateBulletSummary(final IMeetingData meetingData) {
        final String prompt = createBulletSummaryPrompt(meetingData.getChatHistory());
        return llmService.generateContent(prompt);
    }


    private String generateBulletSummary(final IMeetingData meetingData, final int maxMessages) {
        final String prompt = createBulletSummaryPrompt(meetingData.getChatHistory(maxMessages));
        return llmService.generateContent(prompt);
    }


    private String createParagraphSummaryPrompt(final String chatHistory) {
        return String.format(
                "Based on the following meeting discussion, create a natural paragraph summary "
                        + "that captures the flow of conversation and key points discussed.\n\n"
                        + "Write it in a way that shows who said what and how the conversation progressed. "
                        + "Make it sound like a natural narrative of the meeting.\n\n"
                        + "Meeting discussion:\n%s\n\n"
                        + "Please provide a paragraph-style summary:",
                chatHistory
        );
    }


    private String createBulletSummaryPrompt(final String chatHistory) {
        return String.format(
                "Based on the following meeting discussion, create a concise summary with bullet points. "
                        + "Focus on the main discussion points, decisions made, and key takeaways.\n\n"
                        + "Meeting discussion:\n%s\n\n"
                        + "Please provide a bullet-point summary:",
                chatHistory
        );
    }
}