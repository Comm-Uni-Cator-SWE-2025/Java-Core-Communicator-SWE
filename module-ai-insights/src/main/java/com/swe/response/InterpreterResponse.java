/**
 * Author : Abhirami R Iyer
 */
package response;

import java.util.HashMap;
import java.util.Map;

/**
 * InterpreterResponse, represents the image interpretation.
 */
public class InterpreterResponse implements AiResponse {
    /**
     * Type stores the type of the particular response.
     */
    private String type;

    /**
     *  metadata would contain the response details.
     */
    private Map<String, String> metaData;

    /**
     * Constructs an InterpreterResponse and initializes it to a default type.
     */
    public InterpreterResponse() {
        type = "Description Response";
        metaData = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponse() {
        // to return the contents of the response
        return metaData.get("Content");
    }

    /**
     * {@inheritDoc}
     *
     * @param content the content to set in the response
     */
    @Override
    public void setResponse(final String content) {
        // to set the content incase of recieving resposne from the Ai model
        metaData.put("Content", content);
    }

}
