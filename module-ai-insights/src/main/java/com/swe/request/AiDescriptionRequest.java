/**
 * Author : Abhirami R Iyer
 */
package request;

import data.WhiteBoardData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AIDescriptionRequest class inherits the IAIRequest.
 * Stores the metadata of the request to be made to the AI.
 */
public class AiDescriptionRequest implements AiRequestable {
    /**
     * metadata would store prompt.
     * Also, other details of the request like the content.
     */
    private Map<String, String> metaData;
    /**
     * type stores the type of request.
     * type = "DESC"
     */
    private String type;

    /**
     * Constructs an AIDescriptionRequest.
     * Initializes the metadata with a default prompt.
     * the default prompt corresponds to asking for a description.
     * @param inputData gets the image/whiteboard data
     */
    public AiDescriptionRequest(
            final WhiteBoardData inputData) throws IOException {
        // constructor, initialised the metadata,
        // adding the prompt(default prompt for interpretation)
        metaData = new HashMap<>();
        metaData.put("InputData", inputData.getContent());
        metaData.put("RequestPrompt", "Describe this image in detail");
        type = "DESC";
    }

    /**
     * Constructs an AIDescriptionRequest.
     * Initializes the metadata with a prompt given by the user.
     * @param inputData gets the image data
     * @param prompt gets the prompt given by the user
     * @throws IOException in case of any exception
     */
    public AiDescriptionRequest(final WhiteBoardData inputData,
                                final String prompt) throws IOException {
        // constructor, initialised the metadata,
        // adding the prompt (if given by the user).
        metaData = new HashMap<>();
        metaData.put("InputData", inputData.getContent());
        metaData.put("RequestPrompt", prompt);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getContext() {
        // this function, returns the prompt.
        return metaData.get("RequestPrompt");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInput() {
        // this function returns the input.
        return metaData.get("InputData");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReqType() {
        // returns "DESC" as this holds the description request
        return type;
    }


}
