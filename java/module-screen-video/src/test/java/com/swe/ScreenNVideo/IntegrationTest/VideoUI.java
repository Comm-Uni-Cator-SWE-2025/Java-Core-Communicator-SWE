package com.swe.ScreenNVideo.IntegrationTest;

import com.swe.ScreenNVideo.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class VideoUI extends Application {

    private static ImageView imageView;
    private static long start = 0;

    // flag to track if an update is in progress
    private static final AtomicBoolean updating = new AtomicBoolean(false);

    @Override
    public void start(Stage stage) {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Utils.SERVER_WIDTH);
        imageView.setFitHeight(Utils.SERVER_HEIGHT);

        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, Utils.SERVER_WIDTH, Utils.SERVER_HEIGHT);

        stage.setTitle("Screen & Video Display");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Display a frame from byte[] (converted to int[][] outside).
     * Drops new frames if the previous one is still being processed.
     */
    public static void displayFrame(int[][] pixels) {
        if (imageView == null) {
            System.err.println("No Image View");
            return;
        }

        // if already updating, drop this frame
        if (!updating.compareAndSet(false, true)) {
            return;
        }

        int height = pixels.length;
        int width = pixels[0].length;

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pw = writableImage.getPixelWriter();

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                pw.setArgb(y, x, pixels[x][y]);
            }
        }

        Platform.runLater(() -> {
            try {
                System.out.println("Client FPS : "  + (int)(1000.0 / ((System.nanoTime() - start) / 1_000_000.0)) );
                imageView.setImage(writableImage);
                start = System.nanoTime();
            } finally {
                // release flag so next frame can proceed
                updating.set(false);
            }
        });
    }
}
