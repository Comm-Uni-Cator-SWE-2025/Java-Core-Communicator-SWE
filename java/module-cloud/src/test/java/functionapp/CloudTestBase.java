package functionapp;

import static org.mockito.Mockito.*;

import com.microsoft.azure.functions.*;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class CloudTestBase {

    protected ExecutionContext mockContext() {
        ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();
        return context;
    }

    protected <T> HttpRequestMessage<Optional<String>> mockRequest(String body) {
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);
        doReturn(Optional.ofNullable(body)).when(request).getBody();

        doAnswer(invocation -> {
            HttpStatus status = (HttpStatus) invocation.getArguments()[0];
            return new FakeResponseBuilder().status(status);
        }).when(request).createResponseBuilder(any(HttpStatus.class));

        return request;
    }
}
