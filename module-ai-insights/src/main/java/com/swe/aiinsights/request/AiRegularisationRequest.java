/**
 * Author : Abhirami R Iyer
 */
package com.swe.aiinsights.request;

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
        metaData.put("RequestPrompt",
                "You are given a list of 2D points "
                        + "representing a freehand drawing. "
                        + "Your task: identify the most likely "
                        + "geometric shape approximated "
                        + "by these points (Ellipse, Square, "
                        + "Triangle, Rectangle, StraightLine). "
                        + "Then compute the top-left and "
                        + "bottom-right coordinates of the smallest "
                        + "bounding rectangle containing that shape. "

                        + "Return ONLY a JSON object with this exact structure: "
                        + "{ "
                        + "\"ShapeId\": \"<same value>\","
                        + "\"type\": \"<ShapeName>\","
                        + "\"Points\": ["
                        + "{\"x\": <number>, \"y\": <number>},"
                        + "{\"x\": <number>, \"y\": <number>} "
                        + "],"
                        + "\"Color\": \"<same value>\","
                        + "\"Thickness\": \"<same value>\","
                        + "\"CreatedBy\": \"<same value>\","
                        + "\"LastModifiedBy\": \"<same value>\","
                        + "\"IsDeleted\": <same value> "
                        + "} "

                        + "Do not include any extra text, explanation, or alternative formats. "
                        + "Use only the points provided below: \n\n"
                        + points
        );

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
