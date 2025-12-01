/******************************************************************************
 * Filename    = CrashHandler.java
 * Author      = Sooryanarayanan Ganesh
 * Project     = Comm-Uni-Cator
 * Description = Handles crashes and stores exceptions with java crash handler.
 *****************************************************************************/

package crashhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datastructures.Entity;
import datastructures.CloudResponse;
import functionlibrary.CloudFunctionLibrary;
import interfaces.ICrashHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

/**
 * Class which handles logging and storing call stack and other exception
 * details in the application.
 */
public class CrashHandler implements ICrashHandler {

    /** Variable to prevent redundant usage of setDefaultUncaughtExceptionHandler. */
    private static boolean isCreated = false;

    /** CloudFunctionLibrary for crashhandler. */
    private final CloudFunctionLibrary cloudFunctionLibrary;

    /** Collection to which the logs get stored. */
    private static final String COLLECTION = "Exception";

    /** The Id field which is required while storing data. */
    private static int exceptionId = 1;

    /** Return code for exception on create or get function calls. */
    private static final int SUCCESS_CODE = 200;

    /** Return code for startCrashHandler, default to zero. */
    private int returnCode = 0;

    public CrashHandler(final CloudFunctionLibrary cloudFunctionLibraryParam) {
        this.cloudFunctionLibrary = cloudFunctionLibraryParam;
    }

    /**
     * Function which starts the exception handler and handles the logging logic.
     */
    public synchronized void startCrashHandler() {
        // Exit if already set to prevent multiple usage.
        if (isCreated) {
            return;
        }

        isCreated = true;

        final InsightProvider insightProvider = new InsightProvider();

        try {
            final CloudResponse responseCreate = cloudFunctionLibrary.cloudCreate(
                    new Entity(
                            "CLOUD",
                            "ExceptionLogs",
                            null,
                            null,
                            -1,
                            null,
                            null
                    )
            ).join();
            final CloudResponse responseGet = cloudFunctionLibrary.cloudGet(
                    new Entity(
                            "CLOUD",
                            "ExceptionLogs",
                            null,
                            null,
                            1,
                            null,
                            null
                    )
            ).join();

            if (responseCreate.status_code() != SUCCESS_CODE || responseGet.status_code() != SUCCESS_CODE) {
                throw new RuntimeException("Cloud Error...");
            }

            exceptionId = responseGet.data().get(0).get("id").asInt();
        } catch (Exception e) {
            // Do nothing...
        }

        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            final String exceptionName = exception.getClass().getName();
            final String timestamp = Instant.now().toString();
            final String exceptionMessage = exception.getMessage();
            final String exceptionString = exceptionToString(exception);
            final String stackJoined = stackToSingleString(exception.getStackTrace());

            final JsonNode exceptionJsonNode = toJsonNode(
                    exceptionName,
                    timestamp,
                    exceptionMessage,
                    exceptionString,
                    stackJoined
            );

            try {
                final String response = insightProvider.getInsights(exceptionJsonNode.toString());
                ((ObjectNode) exceptionJsonNode).put("AIResponse", response);
                storeDataToFile(exceptionJsonNode.toString());
                final Entity exceptionEntity = new Entity(
                        "CLOUD",
                        "ExceptionLogs",
                        Integer.toString(++exceptionId),
                        null,
                        -1,
                        null,
                        exceptionJsonNode
                );
                final CloudResponse responsePost = cloudFunctionLibrary.cloudPost(exceptionEntity).join();
            } catch (Exception e) {
                // Do nothing...
            }
        });
    }

    private static String exceptionToString(final Throwable ex) {
        final StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String stackToSingleString(final StackTraceElement[] elements) {
        final StringBuilder sb = new StringBuilder();
        for (StackTraceElement e : elements) {
            sb.append(e).append("\n");
        }
        return sb.toString();
    }

    private JsonNode toJsonNode(final String eName, final String timeStamp, final String eMsg,
                                final String eDetails, final String eTrace) {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.createObjectNode()
                .put("ExceptionName", eName)
                .put("TimeStampUtc", timeStamp)
                .put("ExceptionMessage", eMsg)
                .put("ExceptionDetails", eDetails)
                .put("StackTrace", eTrace);

        return jsonNode;
    }

    private void storeDataToFile(final String data) throws IOException {
        final FileWriter fileWriter = new FileWriter("exception_log.jsonl", true);
        fileWriter.write(data + System.lineSeparator());
        fileWriter.close();
    }

}