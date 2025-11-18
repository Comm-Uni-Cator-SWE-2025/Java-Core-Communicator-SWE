package com.swe.aiinsights.main;

import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.aiinsights.aiinstance.AiInstance;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


import java.io.IOException;

/**
 * Main class for running the Image Interpreter application.
 *
 * <p>This class loads the necessary classes,
 * prepares the request, and calls the Gemini API.
 * </p>
 */
public class Main {
    /**
     * Entry point of application.
     *
     * @param args  arguments of main
     * @throws IOException throws error if any of the implementation fails
     */
    public static void main(final String[] args) throws IOException, URISyntaxException {
        AiClientService service = AiInstance.getInstance();
        URL url = Main.class.getClassLoader().getResource("images/test.png");
        Path file = Paths.get(url.toURI());
        // CompletableFuture<String> resp = service.describe(file);

        ObjectMapper mapper = new ObjectMapper();

        JsonNode chat_data = mapper.readTree("""
                                {
  "messages": [
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:00:00Z",
      "message": "I am really excited about today's class!"
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:01:45Z",
      "message": "I'm glad to hear that. Let's make it a productive session."
    },
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:03:20Z",
      "message": "Lately, I have been feeling a little overwhelmed with assignments."
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:04:50Z",
      "message": "I understand. It's okay to feel that. We can work through it together."
    },
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:06:10Z",
      "message": "Thank you. That makes me feel more supported."
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:07:30Z",
      "message": "You are doing well. Small consistent steps will help."
    },
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:08:55Z",
      "message": "I completed the practice exercises and I feel more confident."
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:10:22Z",
      "message": "That's excellent! Your effort is showing great results."
    },
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:12:40Z",
      "message": "I still struggle sometimes when problems get harder though."
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:14:00Z",
      "message": "Struggling is part of learning. You are progressing well. Keep going."
    }
  ]
}


                                """);

        CompletableFuture<String> resp = service.sentiment(chat_data);
        resp.thenAccept(response -> {System.out.println(response);});

        
        // CompletableFuture<String> reg = service.regularise(points);
        // reg.thenAccept(System.out::println);
        // resp.thenAccept(System.out::println);
        System.out.println("AI Process - Running in another thread");


    }
}
