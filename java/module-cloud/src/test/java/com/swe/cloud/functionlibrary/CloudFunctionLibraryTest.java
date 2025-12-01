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

class CloudFunctionLibraryTest {
    CloudFunctionLibrary testCloudFunctionLibrary = new CloudFunctionLibrary();

    @Test
    void cloudCreateTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CompletableFuture<CloudResponse> future = testCloudFunctionLibrary.cloudCreate(testEntity);

        CloudResponse response = future.get();   // blocking only in tests

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    @Test
    void cloudDeleteTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CloudResponse response =testCloudFunctionLibrary.cloudDelete(testEntity).get();

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    @Test
    void cloudGetTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CloudResponse response = testCloudFunctionLibrary.cloudGet(testEntity).get();

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    @Test
    void cloudPostTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CloudResponse response = testCloudFunctionLibrary.cloudPost(testEntity).get();

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

    @Test
    void cloudUpdateTest() throws ExecutionException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId",
                null, -1, new TimeRange(0, 0), null);

        CloudResponse response = testCloudFunctionLibrary.cloudUpdate(testEntity).get();

        assertNotNull(response);
        assertInstanceOf(CloudResponse.class, response);
    }

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

        Throwable cause = exception.getCause();
        assertNotNull(cause);
        assertInstanceOf(IllegalArgumentException.class, cause);
        assertTrue(cause.getMessage().contains("Unsupported HTTP method"));
    }
}