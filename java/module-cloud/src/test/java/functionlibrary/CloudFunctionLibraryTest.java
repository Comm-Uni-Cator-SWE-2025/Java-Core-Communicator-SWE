package functionlibrary;

import datastructures.Entity;
import datastructures.TimeRange;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class CloudFunctionLibraryTest {
    CloudFunctionLibrary testCloudFunctionLibrary = new CloudFunctionLibrary();

    @Test
    void cloudCreateTest() throws IOException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId", null, -1, new TimeRange(0, 0), null);
        Record record = testCloudFunctionLibrary.cloudCreate(testEntity);
        assertInstanceOf(Record.class, record);
    }

    @Test
    void cloudDeleteTest() throws IOException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId", null, -1, new TimeRange(0, 0), null);
        assertInstanceOf(Record.class, testCloudFunctionLibrary.cloudDelete(testEntity));
    }

    @Test
    void cloudGetTest() throws IOException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId", null, -1, new TimeRange(0, 0), null);
        assertInstanceOf(Record.class, testCloudFunctionLibrary.cloudGet(testEntity));
    }

    @Test
    void cloudPostTest() throws IOException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId", null, -1, new TimeRange(0, 0), null);
        assertInstanceOf(Record.class, testCloudFunctionLibrary.cloudPost(testEntity));
    }

    @Test
    void cloudUpdateTest() throws IOException, InterruptedException {
        Entity testEntity = new Entity("TestModule", "TestTable", "TestId", null, -1, new TimeRange(0, 0), null);
        assertInstanceOf(Record.class, testCloudFunctionLibrary.cloudUpdate(testEntity));
    }

    @Test
    void cloudInvalidTest() throws NoSuchMethodException {
        var method = CloudFunctionLibrary.class.getDeclaredMethod("callAPI", String.class, String.class, String.class);
        method.setAccessible(true);

        CloudFunctionLibrary lib = new CloudFunctionLibrary();

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                method.invoke(lib, "/invalid", "GET", "{}")
        );

        Throwable cause = exception.getCause();
        assertInstanceOf(IllegalArgumentException.class, cause);
        assertTrue(cause.getMessage().contains("Unsupported HTTP method"));
    }
}