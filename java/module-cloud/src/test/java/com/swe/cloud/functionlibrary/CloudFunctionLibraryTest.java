/******************************************************************************
 * Filename    = CloudFunctionLibraryTest.java
 * Author      = kallepally sai kiran
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description =  Unit tests for CloudFunctionLibrary asynchronous API wrappers.
 *****************************************************************************/

package functionlibrary;

import datastructures.CloudResponse;
import datastructures.Entity;
import datastructures.TimeRange;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for verifying correctness and behavior of CloudFunctionLibrary.
 *
 * These tests ensure:
 * - Each async wrapper method returns a valid CloudResponse object.
 * - Async calls properly deserialize JSON responses.
 * - Invalid HTTP methods are rejected by callAPIAsync().
 */
class CloudFunctionLibraryTest {

    /** Instance of the library under test. */
    CloudFunctionLibrary testCloudFunctionLibrary = new CloudFunctionLibrary();

    /**
     * Tests the /cloudcreate asynchronous wrapper.
     *
     * Ensures that:
     * - The future completes successfully.
     * - A valid CloudResponse object is returned.
     */
    @Test
    void cloudCreateTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CompletableFuture<CloudResponse> future = testCloudFunctionLibrary.cloudCreate(testEntity);

        CloudResponse response = future.get();   // blocking only in tests

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    /**
     * Tests the /clouddelete asynchronous wrapper.
     * Validates successful response deserialization.
     */
    @Test
    void cloudDeleteTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CloudResponse response =testCloudFunctionLibrary.cloudDelete(testEntity).get();

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    /**
     * Tests the /cloudget asynchronous wrapper.
     * Ensures the API call returns a non-null response.
     */
    @Test
    void cloudGetTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CloudResponse response = testCloudFunctionLibrary.cloudGet(testEntity).get();

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    /**
     * Tests the /cloudpost asynchronous wrapper.
     */
    @Test
    void cloudPostTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CloudResponse response = testCloudFunctionLibrary.cloudPost(testEntity).get();

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    /**
     * Tests the /cloudupdate asynchronous wrapper.
     * Verifies that a PUT operation works correctly.
     */
    @Test
    void cloudUpdateTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CloudResponse response = testCloudFunctionLibrary.cloudUpdate(testEntity).get();

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    /**
     * Tests the private callAPIAsync() method using reflection.
     *
     * Verifies that:
     * - Supplying an unsupported HTTP method (GET) results in an IllegalArgumentException.
     * - The error is properly wrapped in InvocationTargetException due to reflection.
     */
    @Test
    void cloudInvalidTest() throws Exception {
        Method method = CloudFunctionLibrary.class.getDeclaredMethod("callAPIAsync", String.class, String.class, String.class);
        method.setAccessible(true);

        CloudFunctionLibrary lib = new CloudFunctionLibrary();

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> {
                    CompletableFuture<?> future =
                            (CompletableFuture<?>) method.invoke(lib, "/invalid", "GET", "{}");

                    // force evaluation to cause the exception
                    future.get();
                }
        );

        // Validate exception details
        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertInstanceOf(IllegalArgumentException.class, cause);
        assertTrue(cause.getMessage().contains("Unsupported HTTP method"));
    }

    /**
     * Tests the asynchronous sendLog() method.
     *
     * Ensures:
     * - The CompletableFuture completes successfully.
     * - No exceptions are thrown during the async execution.
     * - Logger API accepts the payload structure.
     */
    @Test
    void sendLogTest() throws ExecutionException, InterruptedException {
        CloudFunctionLibrary lib = new CloudFunctionLibrary();

        CompletableFuture<Void> future = lib.sendLog(
                "TestModule",
                "INFO",
                "Unit test log message"
        );

        // force the future to complete (only blocking during test)
        future.get();

        // If no exception -> success
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

}