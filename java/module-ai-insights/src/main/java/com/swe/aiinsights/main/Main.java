//package com.swe.aiinsights.main;
//
// import java.io.IOException;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.concurrent.CompletableFuture;
//
//
// import com.swe.aiinsights.apiendpoints.AiClientService;
// import com.swe.aiinsights.aiinstance.AiInstance;
// import com.swe.aiinsights.parser.InsightsParser;
//
// import java.net.URISyntaxException;
//
////
////
//import java.io.IOException;
////
/////**
//// * Main class for running the Image Interpreter application.
//// *
//// * <p>This class loads the necessary classes,
//// * prepares the request, and calls the Gemini API.
//// * </p>
//// */
// public class Main {
////
////
////
//
////    /**
////     * Entry point of application.
////     *
////     * @param args  arguments of main
////     * @throws IOException throws error if any of the implementation fails
////     */
//     public static void main(final String[] args) throws Exception {
//       AiClientService service = AiInstance.getInstance();
//
//        String contextualQ = " What is Bob working on?";
//        CompletableFuture<String> answer1 = service.answerQuestion(contextualQ);
//        answer1.thenAccept(System.out::println).join();
//
//        Path path = Paths.get("src", "main", "resources", "images", "test.png");
//
//        CompletableFuture<String> img = service.describe(path.toString());
//
//        img.thenAccept(System.out::println).join();
//
//     }
// }
