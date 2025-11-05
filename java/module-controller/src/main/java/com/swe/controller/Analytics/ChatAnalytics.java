package com.swe.controller.Analytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatAnalytics {

    private String[][] chatData;     //{from, chat_id, to, message, attachment}
    private String aiApiUrl;         // AI API endpoint URL

    public ChatAnalytics(String[][] chatData, String aiApiUrl) {
        this.chatData = chatData;
        this.aiApiUrl = aiApiUrl;
    }

    public String callAI(){
    try  {
        String jsonPayload = convertToJson(chatData);   //chat to Json format

        URL url = new URL(aiApiUrl);                    // HTTP connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try {
            OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.getBytes("utf-8")); // send JSON
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8")
        );
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }

        return response.toString();

    } catch (Exception e) {
        e.printStackTrace();
        return "Error: " + e.getMessage() + ". Check API URL and token.";
    }

    }

    // ✅ Convert 2D array into JSON string manually
    private String convertToJson(String[][] data) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < data.length; i++) {
            sb.append("{");
            sb.append("\"from\":\"").append(data[i][0]).append("\",");
            sb.append("\"chat_id\":\"").append(data[i][1]).append("\",");
            sb.append("\"to\":\"").append(data[i][2]).append("\",");
            sb.append("\"message\":\"").append(data[i][3]).append("\",");
            sb.append("\"attachment\":\"").append(data[i][4]).append("\"");
            sb.append("}");
            if (i < data.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

//    public static void main(String[] args) {
//        String[][] chatArray = {
//                {"u1", "c1", "u2", "Hi!", ""},
//                {"u2", "c2", "u1", "Hello!", "img.png"},
//                {"u3", "c3", "u1", "Need help?", ""}
//        };
//
//        // Dummy API endpoint (replace with real one)
//        String aiUrl = "https://example.com/ai/analyzeChat";
//
//        ChatAnalytics analytics = new ChatAnalytics(chatArray, aiUrl);
//        System.out.println("AI Response: " + analytics.callAI());
//
//        String json = analytics.convertToJson(chatArray);
//        System.out.println("JSON Output:");
//        System.out.println(json);
//    }

}




