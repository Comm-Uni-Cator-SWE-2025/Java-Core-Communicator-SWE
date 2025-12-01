/******************************************************************************
 * Filename    = FakeResponseBuilder.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Mock implementation of HttpResponseMessage.
 *               Builder used for unit testing Azure Functions.
 *****************************************************************************/

package functionapp;

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;

/**
 * Simple fake implementation of HttpResponseMessage.Builder
 * used to simulate Azure Function HTTP responses during tests.
 */
class FakeResponseBuilder implements HttpResponseMessage.Builder {
    private HttpStatus status;
    private Object body;

    /** Builds a minimal HttpResponseMessage instance. */
    @Override
    public HttpResponseMessage build() {
        return new HttpResponseMessage() {
            @Override public HttpStatus getStatus() { return status; }
            @Override public Object getBody() { return body; }
            @Override public String getHeader(String name) { return null; }
        };
    }

    /** Sets the HTTP status code. */
    @Override
    public HttpResponseMessage.Builder status(HttpStatusType statusType) {
        if (statusType instanceof HttpStatus) this.status = (HttpStatus) statusType;
        else this.status = HttpStatus.OK;
        return this;
    }

    /** Sets the response body. */
    @Override
    public HttpResponseMessage.Builder body(Object body) { this.body = body; return this; }
    /** Ignored in tests – returns builder unchanged. */
    @Override
    public HttpResponseMessage.Builder header(String name, String value) { return this; }
}