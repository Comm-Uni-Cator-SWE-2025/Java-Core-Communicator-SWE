package com.Comm_Uni_Cator.chat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Hello world!
 */
//public class App {
//    public static void main(String[] args) {
//        System.out.println("Hello World!");
//    }
//}



// Your main class now extends Application from JavaFX
public class App extends Application {

    // The start method is the main entry point for all JavaFX applications
    @Override
    public void start(Stage primaryStage) {
        try {

            // Load the FXML file we created.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com.Comm_Uni_Cator/chat/ChatView.fxml"));
            Parent root = fxmlLoader.load();


            // Set up the main window (called a "Stage")
            primaryStage.setTitle("CommUniCator Chat");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // The main method now just launches the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}