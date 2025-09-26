package chatsummary;

/**
 * Main class that users interact with - provides easy summary methods.
 */
public class SummaryService implements IAIRequest {
    /** The summarizer instance. */
    private ISummarizer summarizer;

    /**
     * Default constructor that creates a new Summarizer.
     */
    public SummaryService() {
        this.summarizer = new Summarizer();
    }

    /**
     * Constructor that accepts a custom summarizer.
     *
     * @param summarizerInstance the summarizer to use
     */
    public SummaryService(final ISummarizer summarizerInstance) {
        this.summarizer = summarizerInstance;
    }

    @Override
    public String processRequest(final AIRequest request, final IMeetingData meetingData) {
        return summarizer.generateSummary(meetingData, request);
    }

    /**
     * Generate a paragraph-style summary for all meeting data.
     *
     * @param meetingData the meeting data
     * @return generated summary
     */
    public String generateParagraphSummary(final IMeetingData meetingData) {
        final AIRequest request = new AIRequest("SUMMARY", "Generate paragraph-style meeting summary");
        return processRequest(request, meetingData);
    }

    /**
     * Generate a paragraph-style summary for limited messages.
     *
     * @param meetingData the meeting data
     * @param maxMessages maximum number of messages
     * @return generated summary
     */
    public String generateParagraphSummary(final IMeetingData meetingData, final int maxMessages) {
        final AIRequest request = new AIRequest("SUMMARY_LIMITED",
                "Generate paragraph-style summary of last " + maxMessages + " messages",
                maxMessages);
        return processRequest(request, meetingData);
    }

    /**
     * Generate a bullet-point summary for all meeting data.
     *
     * @param meetingData the meeting data
     * @return generated summary
     */
    public String generateBulletSummary(final IMeetingData meetingData) {
        final AIRequest request = new AIRequest("BULLET_SUMMARY", "Generate bullet-point meeting summary");
        return processRequest(request, meetingData);
    }

    /**
     * Generate a bullet-point summary for limited messages.
     *
     * @param meetingData the meeting data
     * @param maxMessages maximum number of messages
     * @return generated summary
     */
    public String generateBulletSummary(final IMeetingData meetingData, final int maxMessages) {
        final AIRequest request = new AIRequest("BULLET_LIMITED",
                "Generate bullet-point summary of last " + maxMessages + " messages",
                maxMessages);
        return processRequest(request, meetingData);
    }
}