/******************************************************************************
 * Filename    = CloudTestBase.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Base class providing common mock utilities for Azure Function unit tests.
 *****************************************************************************/

package functionapp;

import static org.mockito.Mockito.*;

import com.microsoft.azure.functions.*;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Base class providing common mocks for Azure Function tests.
 */
public abstract class CloudTestBase {

    /** Creates a mock ExecutionContext with a global logger. */
    protected ExecutionContext mockContext() {
        ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();
        return context;
    }

    /**
     * Creates a mock HTTP request with the given body.
     * Also mocks createResponseBuilder() to return a FakeResponseBuilder.
     */
    protected <T> HttpRequestMessage<Optional<String>> mockRequest(String body) {
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);
        // Set request body
        doReturn(Optional.ofNullable(body)).when(request).getBody();

        // Mock response builder behavior
        doAnswer(invocation -> {
            HttpStatus status = (HttpStatus) invocation.getArguments()[0];
            return new FakeResponseBuilder().status(status);
        }).when(request).createResponseBuilder(any(HttpStatus.class));

        return request;
    }
}
