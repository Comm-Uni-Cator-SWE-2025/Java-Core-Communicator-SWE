package org.example;

import chatsummary.MeetingData;
import chatsummary.SummaryService;
import chatsummary.IAIRequest;
import chatsummary.IMeetingData;
import chatsummary.AIRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main class that runs the summary program.
 */
public class Main {

    private static final int MAX_MESSAGES_LIMIT = 5;

    public static void main(final String[] args) throws IOException {

        // 1. Create meeting notebook
        final IMeetingData meetingData = new MeetingData();

        // 2. Read meeting from file
        final List<String> lines = Files.readAllLines(Paths.get("meeting.txt"));
        for (String line : lines) {
            final String[] parts = line.split(":", 2); // format: Sender:Message
            if (parts.length == 2) {
                meetingData.addMessage(parts[0].trim(), parts[1].trim());
            }
        }

        // 3. Create summary service
        final IAIRequest summaryService = new SummaryService();

        // 4. Create different types of summary requests
        final AIRequest paragraphSummaryRequest = new AIRequest("SUMMARY",
                "Generate comprehensive paragraph-style meeting summary");

        final AIRequest limitedParagraphRequest = new AIRequest("SUMMARY_LIMITED",
                "Generate paragraph summary of last " + MAX_MESSAGES_LIMIT + " messages", MAX_MESSAGES_LIMIT);

        final AIRequest bulletSummaryRequest = new AIRequest("BULLET_SUMMARY",
                "Generate bullet-point meeting summary");

        final AIRequest limitedBulletRequest = new AIRequest("BULLET_SUMMARY_LIMITED",
                "Generate bullet summary of last " + MAX_MESSAGES_LIMIT + " messages", MAX_MESSAGES_LIMIT);

        // 5. Generate and display all summary types
        System.out.println("=== PARAGRAPH-STYLE MEETING SUMMARY ===");
        final String paragraphSummary = summaryService.processRequest(paragraphSummaryRequest, meetingData);
        System.out.println(paragraphSummary);

        System.out.println("\n=== LIMITED PARAGRAPH SUMMARY (LAST " + MAX_MESSAGES_LIMIT + " MESSAGES) ===");
        final String limitedParagraph = summaryService.processRequest(limitedParagraphRequest, meetingData);
        System.out.println(limitedParagraph);

        System.out.println("\n=== BULLET POINT SUMMARY ===");
        final String bulletSummary = summaryService.processRequest(bulletSummaryRequest, meetingData);
        System.out.println(bulletSummary);

        System.out.println("\n=== LIMITED BULLET SUMMARY (LAST " + MAX_MESSAGES_LIMIT + " MESSAGES) ===");
        final String limitedBullet = summaryService.processRequest(limitedBulletRequest, meetingData);
        System.out.println(limitedBullet);

        // 6. Show meeting statistics
        System.out.println("\n=== MEETING STATISTICS ===");
        System.out.println("Total messages: " + meetingData.getMessageCount());
        System.out.println("Participants: " + meetingData.getParticipants());
    }
}