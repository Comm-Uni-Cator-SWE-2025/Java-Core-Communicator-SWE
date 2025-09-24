package chat_summary;

/**
 * SummaryService class - implements IAIRequest interface as per UML diagram
 * Acts as the main service coordinating between components
 */
public class SummaryService implements IAIRequest {
    private ISummarizer summarizer;

    public SummaryService() {
        this.summarizer = new Summarizer();
    }

    public SummaryService(ISummarizer summarizer) {
        this.summarizer = summarizer;
    }

    @Override
    public String processRequest(AIRequest request, IMeetingData meetingData) {
        // Delegate to summarizer
        return summarizer.generateSummary(meetingData, request);
    }

    // Convenience methods for backward compatibility and direct access
    public String generateParagraphSummary(IMeetingData meetingData) {
        AIRequest request = new AIRequest("SUMMARY", "Generate paragraph-style meeting summary");
        return processRequest(request, meetingData);
    }

    public String generateParagraphSummary(IMeetingData meetingData, int maxMessages) {
        AIRequest request = new AIRequest("SUMMARY_LIMITED",
                "Generate paragraph-style summary of last " + maxMessages + " messages",
                maxMessages);
        return processRequest(request, meetingData);
    }

    public String generateBulletSummary(IMeetingData meetingData) {
        AIRequest request = new AIRequest("BULLET_SUMMARY", "Generate bullet-point meeting summary");
        return processRequest(request, meetingData);
    }

    public String generateBulletSummary(IMeetingData meetingData, int maxMessages) {
        AIRequest request = new AIRequest("BULLET_LIMITED",
                "Generate bullet-point summary of last " + maxMessages + " messages",
                maxMessages);
        return processRequest(request, meetingData);
    }
}