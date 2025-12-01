package functionlibrary;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

class CloudLoggerTest {
    CloudLogger logger = CloudLogger.getLogger("LoggerTest");

    @Test
    void testInfo_LogsLocally() {
        assertDoesNotThrow(() -> {
            logger.info("This is an info message (Local only)").join();
        });
    }

    @Test
    void testWarn_SendsToCloud_Integration() {
        assertDoesNotThrow(() -> {
            logger.warn("This is a warning message (Cloud integration check)").join();
        });
    }

    @Test
    void testError_SendsToCloud_Integration() {
        assertDoesNotThrow(() -> {
            logger.error("This is an error message (Cloud integration check)").join();
        });
    }

    @Test
    void testErrorWithException_SendsFullDetails_Integration() {
        Exception simException = new RuntimeException("error");
        assertDoesNotThrow(() -> {
            logger.error("This is an exception error", simException).join();
        });
    }

}