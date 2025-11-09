package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.EnableAsync;


import java.io.IOException;

/**
 * Main class for running the Image Interpreter application.
 *
 * <p>This class loads the necessary classes,
 * prepares the request, and calls the Gemini API.
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"main", "apiendpoints",
        "aiservice", "configu"})
@EnableAsync
public class Main {
    /**
     * Entry point of application.
     *
     * @param args  arguments of main
     * @throws IOException throws error if any of the implementation fails
     */
    public static void main(final String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
    }
}
