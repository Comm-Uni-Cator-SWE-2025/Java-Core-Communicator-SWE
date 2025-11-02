package com.swe.chat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Hello world!.
 */

//public class App {
//    public static void main(String[] args) {
//        System.out.println("Hello World!");
//    }
//}


import javax.swing.SwingUtilities;

/**
 * Main application launcher for the Swing version.
 * This REPLACES your JavaFX App.java.
 */
public class App {

    public static void main(final String[] args) {
        // Launch all Swing UIs on the Event Dispatch Thread (EDT)
        // This is the standard, required way to start a Swing app.
        SwingUtilities.invokeLater(() -> {
            ChatView chatView = new ChatView();
            chatView.setVisible(true);
        });
    }
}

// Your main class now extends Application from JavaFX
//public class App extends Application {
//
//    // The start method is the main entry point for all JavaFX applications
//    @Override
//    public void start(final Stage primaryStage) {
//        try {
//
//            // Load the FXML file we created.
//            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
//                    "/ChatView.fxml")
//            );
//            final Parent root = fxmlLoader.load();
//
//
//            // Set up the main window (called a "Stage")
//            primaryStage.setTitle("CommUniCator Chat");
//            primaryStage.setScene(new Scene(root));
//            primaryStage.show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // The main method now just launches the JavaFX application
//    public static void main(final String[] args) {
//        launch(args);
//    }
//}