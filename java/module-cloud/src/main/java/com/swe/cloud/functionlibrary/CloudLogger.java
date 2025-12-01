/******************************************************************************
 * Filename    = CloudLogger.java
 * Author      = Sidarth Prabhu
 * Project     = Comm-Uni-Cator
 * Description = Cloud Logger methods for other modules
 *****************************************************************************/

package functionlibrary;

import java.util.concurrent.CompletableFuture;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Custom logger that writes logs to a local file and sends errors/warnings to the cloud.
 */
public class CloudLogger {

    /** Library instance for cloud communication. */
    private static final CloudFunctionLibrary CLOUD_LIB = new CloudFunctionLibrary();

    /** Internal Java logger for local file logging. */
    private static final Logger INTERNAL_LOGGER = Logger.getLogger("CommUniCator");

    static {
        try {
            // Configure local file logging (application.log)
            // 'true' means append mode (don't overwrite on restart)
            final FileHandler fileHandler = new FileHandler("application.log", true);
            fileHandler.setFormatter(new SimpleFormatter());

            // Disable default console output to improve performance
            INTERNAL_LOGGER.setUseParentHandlers(false);
            INTERNAL_LOGGER.addHandler(fileHandler);
            INTERNAL_LOGGER.setLevel(Level.INFO);
        } catch (final Exception e) {
            System.err.println("Logger setup failed: " + e.getMessage());
        }
    }

    /** Name of the module using this logger. */
    private final String moduleName;

    /**
     * Constructor.
     *
     * @param name The name of the module.
     */
    private CloudLogger(final String name) {
        this.moduleName = name;
    }

    /**
     * Factory method to get a logger instance.
     *
     * @param moduleName The name of the module.
     * @return A CloudLogger instance.
     */
    public static CloudLogger getLogger(final String moduleName) {
        return new CloudLogger(moduleName);
    }

    /**
     * Logs an INFO message to the local file only.
     *
     * @param message The message to log.
     * @return A completable future of type void notifying that the job is complete
     */
    public CompletableFuture<Void> info(final String message) {
        return CompletableFuture.runAsync(() ->
                INTERNAL_LOGGER.info("[" + moduleName + "] " + message)
        );
    }

    /**
     * Logs a WARNING message to the local file and sends it to the cloud.
     *
     * @param message The warning message.
     * @return A completable future of type void notifying that the job is complete
     */
    public CompletableFuture<Void> warn(final String message) {
        final CompletableFuture<Void> localLogTask = CompletableFuture.runAsync(() ->
                INTERNAL_LOGGER.warning("[" + moduleName + "] " + message)
        );
        final CompletableFuture<Void> cloudLogTask = CLOUD_LIB.sendLog(moduleName, "WARNING", message);

        return CompletableFuture.allOf(localLogTask, cloudLogTask);
    }

    /**
     * Logs an ERROR message to the local file and sends it to the cloud.
     *
     * @param message The error message.
     * @return A completable future of type void notifying that the job is complete
     */
    public CompletableFuture<Void> error(final String message) {
        final CompletableFuture<Void> localLogTask = CompletableFuture.runAsync(() ->
                INTERNAL_LOGGER.severe("[" + moduleName + "] " + message)
        );

        final CompletableFuture<Void> cloudLogTask = CLOUD_LIB.sendLog(moduleName, "ERROR", message);

        return CompletableFuture.allOf(localLogTask, cloudLogTask);
    }

    /**
     * Logs an ERROR message with an exception to the local file and sends it to the cloud.
     *
     * @param message The error message.
     * @param e       The exception to log.
     * @return A completable future of type void notifying that the job is complete
     */
    public CompletableFuture<Void> error(final String message, final Exception e) {
        String exceptionDetails = "";
        if (e != null) {
            exceptionDetails = " | Exception: " + e.toString();
        }
        final String fullMessage = message + exceptionDetails;

        final CompletableFuture<Void> localLogTask = CompletableFuture.runAsync(() ->
                INTERNAL_LOGGER.severe("[" + moduleName + "] " + fullMessage)
        );

        final CompletableFuture<Void> cloudLogTask = CLOUD_LIB.sendLog(moduleName, "ERROR", fullMessage);

        return CompletableFuture.allOf(localLogTask, cloudLogTask);
    }
}