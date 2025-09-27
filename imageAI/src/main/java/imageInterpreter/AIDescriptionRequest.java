/**
 * This package contains classes for interpreting images.
 * On interpreting the images,  AI-based descriptions are generated and shown as the output.
 */

package imageinterpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * AIDescriptionRequest class inherits the IAIRequest.
 * Stores the metadata of the request to be made to the AI.
 */
public class AIDescriptionRequest implements IAIRequest {
    /**
     * metadata would store prompt, and other details of the request like the content.
     */
    private Map<String, String> metaData;

    /**
     * Constructs an AIDescriptionRequest and initializes the metadata with a default prompt.
     * the default prompt corresponds to asking for a description.
     */
    public AIDescriptionRequest() {
        // constructor, initialised the metadata, adding the prompt.
        metaData = new HashMap<>();
        metaData.put("RequestPrompt", "Describe this image in detail");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContext() {
        // this function, returns the prompt.
        return metaData.get("RequestPrompt");
    }
}
