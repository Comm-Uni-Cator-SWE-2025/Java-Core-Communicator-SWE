package com.swe.ScreenNVideo.IntegrationTest;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class VideoUI extends Application {

    private static ImageView imageView;

    @Override
    public void start(Stage stage) {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);  // adjust as needed
        imageView.setFitHeight(600);

        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Screen & Video Display");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Display a frame from byte[] (converted to int[][] outside).
     */
    public static void displayFrame(int[][] pixels) {
        if (imageView == null) return;

        int width = pixels.length;
        int height = pixels[0].length;

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pw = writableImage.getPixelWriter();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pw.setArgb(x, y, pixels[x][y]); // assumes pixels contain ARGB ints
            }
        }

        Platform.runLater(() -> imageView.setImage(writableImage));
    }
}
