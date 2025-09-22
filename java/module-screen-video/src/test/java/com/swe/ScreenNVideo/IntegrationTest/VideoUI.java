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
    private static long start = 0;

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
        if (imageView == null) {
            System.err.println("No Image View");
            return;
        }

        int height = pixels.length;
        int width = pixels[0].length;

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pw = writableImage.getPixelWriter();

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                pw.setArgb(y, x, pixels[x][y]); // assumes pixels contain ARGB ints
            }
        }

        Platform.runLater(() -> {
            System.out.println((System.nanoTime() - start) / 1_000_000.0 + " ms");
            imageView.setImage(writableImage);
            start = System.nanoTime();
        });
    }
}
