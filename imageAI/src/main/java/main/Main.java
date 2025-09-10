package main;

import imageInterpreter.*;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        // should be done through env files
        String geminiApiKey = "AIzaSyCDOP4weSKku50w-UPQxAzyVbH7m9rMYTQ";

        WhiteBoardData whiteboardData = new WhiteBoardData("C://temp//image.png");

        IAIRequest descriptionRequest = new AIDescriptionRequest();

        IImageInterpreter imageInterpreter = new ImageInterpreterCloud(geminiApiKey);

        try{
            System.out.println("Calling the describe image function, to get the interpretation");

            IAIResponse interpretation = imageInterpreter.describeImage(descriptionRequest, whiteboardData);

            System.out.println("\n ---- GEMINI INTERPRETATION ----- \n");

            System.out.println(interpretation.getResponse());

            System.out.println("\n ---- GEMINI INTERPRETATION ----- \n");
        }
        catch (IOException e){
            System.err.println("An error occurred");
            e.printStackTrace();
        }
    }
}