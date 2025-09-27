package main;

import imageinterpreter.IImageInterpreter;
import imageinterpreter.WhiteBoardData;
import imageinterpreter.AIDescriptionRequest;
import imageinterpreter.ImageInterpreterCloud;
import imageinterpreter.IAIResponse;
import imageinterpreter.IAIRequest;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;

/**
 * Main class for running the Image Interpreter application.
 *
 * <p>This class loads the necessary classes, prepares the request, and calls the Gemini API.
 */
public class Main {
    /**
     * Entry point of application.
     *
     * @param args  arguments of main
     * @throws IOException throws error if any of the implemenation fails
     */
    public static void main(final String[] args) throws IOException {
        /**
         * gemini api key is fetched from the .env file.
         */
        final  Dotenv dotenv = Dotenv.load();

        /**
         * File path is also fetched.
         */
        final String geminiApiKey = dotenv.get("GEMINI_API_KEY");

        /**
         * The image is converted to a WhiteBoardDataResponse.
         */
        final String imagePath = dotenv.get("IMAGE_PATH");
        final WhiteBoardData whiteboardData = new WhiteBoardData(imagePath);

        /**
         * The whiteBoardDataResponse, is added to the request(description request)
         */
        final IAIRequest descriptionRequest = new AIDescriptionRequest();

        /**
         * the AIRequest is used by the imageInterpreter, to send the request to Gemini.
         */
        final IImageInterpreter imageInterpreter = new ImageInterpreterCloud(geminiApiKey);

        try {
            System.out.println("Calling the describe image function, to get the interpretation");

            final IAIResponse interpretation = imageInterpreter.describeImage(descriptionRequest, whiteboardData);

            System.out.println("\n ---- GEMINI INTERPRETATION ----- \n");

            System.out.println(interpretation.getResponse());

            System.out.println("\n ---- GEMINI INTERPRETATION ----- \n");

        } catch (IOException e) {
            System.err.println("An error occurred");
            e.printStackTrace();
        }
    }
}