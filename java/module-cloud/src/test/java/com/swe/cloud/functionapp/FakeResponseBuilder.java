package functionapp;

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;

class FakeResponseBuilder implements HttpResponseMessage.Builder {
    private HttpStatus status;
    private Object body;

    @Override
    public HttpResponseMessage build() {
        return new HttpResponseMessage() {
            @Override public HttpStatus getStatus() { return status; }
            @Override public Object getBody() { return body; }
            @Override public String getHeader(String name) { return null; }
        };
    }

    @Override
    public HttpResponseMessage.Builder status(HttpStatusType statusType) {
        if (statusType instanceof HttpStatus) this.status = (HttpStatus) statusType;
        else this.status = HttpStatus.OK;
        return this;
    }

    @Override
    public HttpResponseMessage.Builder body(Object body) { this.body = body; return this; }
    @Override
    public HttpResponseMessage.Builder header(String name, String value) { return this; }
}