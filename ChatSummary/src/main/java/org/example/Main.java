package org.example;

// Import classes from the chat_summary package
import chat_summary.MeetingData;
import chat_summary.SummaryService;
import chat_summary.AIRequest;
import chat_summary.IMeetingData;
import chat_summary.IAIRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        // Create meeting data using interface as per UML
        IMeetingData meetingData = new MeetingData();

        // Read meeting data from file - using your meeting.txt file
        List<String> lines = Files.readAllLines(Paths.get("meeting.txt"));
        for (String line : lines) {
            String[] parts = line.split(":", 2); // format: Sender:Message
            if (parts.length == 2) {
                meetingData.addMessage(parts[0].trim(), parts[1].trim());
            }
        }

        // Create AI request processor using interface as per UML
        IAIRequest summaryService = new SummaryService();

        // Create AI requests as per UML structure
        AIRequest paragraphSummaryRequest = new AIRequest("SUMMARY",
                "Generate comprehensive paragraph-style meeting summary");

        AIRequest limitedParagraphRequest = new AIRequest("SUMMARY_LIMITED",
                "Generate paragraph summary of last 5 messages", 5);

        AIRequest bulletSummaryRequest = new AIRequest("BULLET_SUMMARY",
                "Generate bullet-point meeting summary");

        AIRequest limitedBulletRequest = new AIRequest("BULLET_SUMMARY_LIMITED",
                "Generate bullet summary of last 5 messages", 5);

        // Process requests through the interface
        System.out.println("=== PARAGRAPH-STYLE MEETING SUMMARY ===");
        String paragraphSummary = summaryService.processRequest(paragraphSummaryRequest, meetingData);
        System.out.println(paragraphSummary);

        System.out.println("\n=== LIMITED PARAGRAPH SUMMARY (LAST 5 MESSAGES) ===");
        String limitedParagraph = summaryService.processRequest(limitedParagraphRequest, meetingData);
        System.out.println(limitedParagraph);

        System.out.println("\n=== BULLET POINT SUMMARY ===");
        String bulletSummary = summaryService.processRequest(bulletSummaryRequest, meetingData);
        System.out.println(bulletSummary);

        System.out.println("\n=== LIMITED BULLET SUMMARY (LAST 5 MESSAGES) ===");
        String limitedBullet = summaryService.processRequest(limitedBulletRequest, meetingData);
        System.out.println(limitedBullet);

        // Display meeting statistics
        System.out.println("\n=== MEETING STATISTICS ===");
        System.out.println("Total messages: " + meetingData.getMessageCount());
        System.out.println("Participants: " + meetingData.getParticipants());
    }
}