package chatsummary;

/**
 * Main class that users interact with - provides easy summary methods.
 */
public class SummaryService implements IAIRequest {

    private ISummarizer summarizer;


    public SummaryService() {
        this.summarizer = new Summarizer();
    }


    public SummaryService(final ISummarizer summarizerInstance) {
        this.summarizer = summarizerInstance;
    }

    @Override
    public String processRequest(final AIRequest request, final IMeetingData meetingData) {
        return summarizer.generateSummary(meetingData, request);
    }

    public String generateParagraphSummary(final IMeetingData meetingData) {
        final AIRequest request = new AIRequest("SUMMARY", "Generate paragraph-style meeting summary");
        return processRequest(request, meetingData);
    }

    public String generateParagraphSummary(final IMeetingData meetingData, final int maxMessages) {
        final AIRequest request = new AIRequest("SUMMARY_LIMITED",
                "Generate paragraph-style summary of last " + maxMessages + " messages",
                maxMessages);
        return processRequest(request, meetingData);
    }

    public String generateBulletSummary(final IMeetingData meetingData) {
        final AIRequest request = new AIRequest("BULLET_SUMMARY", "Generate bullet-point meeting summary");
        return processRequest(request, meetingData);
    }

    public String generateBulletSummary(final IMeetingData meetingData, final int maxMessages) {
        final AIRequest request = new AIRequest("BULLET_LIMITED",
                "Generate bullet-point summary of last " + maxMessages + " messages",
                maxMessages);
        return processRequest(request, meetingData);
    }
}