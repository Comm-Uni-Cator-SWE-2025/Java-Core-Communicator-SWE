/**
 * Author : Abhirami R Iyer
 */
package request;

import java.util.HashMap;
import java.util.Map;

/**
 * AIRegularisationRequest class inherits the IAIRequest.
 * Stores the metadata of the request to be made to the AI.
 */
public class AiRegularisationRequest implements AiRequestable {
    /**
     * metadata would store prompt, and other
     * details of the request like the content.
     */
    private Map<String, String> metaData;
    /**
     * holds the type of request.
     * type = "REG"
     */
    private String type;
    /**
     * Constructs an AIRegularisationRequest and
     * initializes the metadata with a default prompt.
     * the default prompt corresponds to asking for
     * a regularising to the nearest shape.
     * @param points to store the string
     *               containing points of the curve for regularisation
     */
    public AiRegularisationRequest(final String points) {
        // constructor, initialised the metadata,
        // adding the prompt.
        metaData = new HashMap<>();
        metaData.put("inputData", points);
        metaData.put("RequestPrompt", "Given a list of 2D "
                + "points representing a freehand drawing:" + points
                + ". Analyze these points to identify the most "
                + "likely regular geometric shape "
                + "they approximate (types allowed: Ellipse, "
                + "Square, Triangle, Rectangle, Straight line)."
                + " Then, determine the coordinates of the top-left and "
                + "bottom-right corners of the smallest "
                + "bounding rectangle that fully contains this shape. \n" + "\n"
                + "Return the result strictly in JSON format and do not "
                + "include any explanation or extra text."
                + " The output JSON must have the following structure:\n"
                + "{\n" + "  \"type\": \"ShapeName\",\n"
                + "  \"top_left\": {\"x\": number, \"y\": number},\n"
                + "  \"bottom_right\": {\"x\": number, \"y\": number}\n"
                + "}\n" + "\n" + "Example output:\n"
                + "{\n" + "  \"type\": \"Circle\",\n"
                + "  \"top_left\": {\"x\": 10.0, \"y\": 20.0},\n"
                + "  \"bottom_right\": {\"x\": 50.0, \"y\": 70.0}\n"
                + "}\n");
        type = "REG";
    }

    /**
     * Constructs an AIRegularisationRequest and
     * initializes the metadata with a default prompt.
     * the default prompt corresponds to asking for
     * a regularising to the nearest shape.
     * @param points to store the string
     *               containing points of the curve for regularisation
     * @param prompt to get the prompt if any
     */
    public AiRegularisationRequest(final String points, final String prompt) {
        // constructor, initialised the metadata,
        // adding the prompt.
        metaData = new HashMap<>();
        metaData.put("InputData", points);
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
        // this returns "REG" as this holds
        // the regularization request
        return type;
    }
}
