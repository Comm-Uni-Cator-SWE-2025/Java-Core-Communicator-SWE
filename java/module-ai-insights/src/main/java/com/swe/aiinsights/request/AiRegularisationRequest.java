/*
 * -----------------------------------------------------------------------------
 *  File: AiRegularisationRequest.java
 *  Owner:Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.request
 * -----------------------------------------------------------------------------
 */

/**
 * Stores the AI request, for regularisation.
 * prompt, and the input data of points are stored.
 *
 * @author Abhirami R Iyer
 */

package com.swe.aiinsights.request;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * AIRegularisationRequest class inherits the IAIRequest.
 * Stores the metadata of the request to be made to the AI.
 */
public class AiRegularisationRequest implements AiRequestable {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG =
            SweLoggerFactory.getLogger("AI-INSIGHTS");
    /**
     * metadata would store prompt, and other
     * details of the request like the content.
     */
    private Map<String, String> metaData;
    /**
     * holds the type of request.
     * type = "REG"
     */
    private String type = "REG";
    /**
     * Constructs an AIRegularisationRequest and
     * initializes the metadata with a default prompt.
     * the default prompt corresponds to asking for
     * a regularising to the nearest shape.
     *
     * @param points to store the string
     *               containing points of the curve for regularisation
     */

    public AiRegularisationRequest(final String points) {
        // constructor, initialised the metadata,
        // adding the prompt.
        metaData = new HashMap<>();
        metaData.put("InputData", points);
        metaData.put("RequestPrompt", """
                "Given the list of (X, Y) coordinates below, which represent a freehand drawing, analyze them
                 to identify the underlying geometric shape that best fits the drawing:
                  **RECTANGLE**, **ELLIPSE**, **TRIANGLE**, or **LINE**.

                 Then, generate a single, complete **JSON string** that represents the processed shape.
                  This output must strictly adhere to the following rules:

                 1.  Replace the original shape `Type` (e.g., 'FREEHAND') with the **identified shape** (e.g., 'RECTANGLE').
                 2.  Replace the entire original `Points` list with only **two** new point objects:
                     * The **top-left corner** $(X_{\\text{min}}, Y_{\\text{min}})$ of the minimum bounding rectangle.
                     * The **bottom-right corner** $(X_{\\text{max}}, Y_{\\text{max}})$ of the minimum bounding rectangle.
                 3.  **Do not modify** the `ShapeId`, `Color`, `Thickness`, `CreatedBy`, `LastModifiedBy`, or `IsDeleted` fields.
                 4.  **The output must be a JSON string and nothing else.**
                """);

        type = "REG";
    }

//    /**
//     * Constructs an AIRegularisationRequest and
//     * initializes the metadata with a default prompt.
//     * the default prompt corresponds to asking for
//     * a regularising to the nearest shape.
//     * @param points to store the string
//     *               containing points of the curve for regularisation
//     * @param prompt to get the prompt if any
//     */
//    public AiRegularisationRequest(final String points, final String prompt) {
//        // constructor, initialised the metadata,
//        // adding the prompt.
//        metaData = new HashMap<>();
//        metaData.put("InputData", points);
//        metaData.put("RequestPrompt", prompt);
//        type = "REG";
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContext() {
        // this function, returns the prompt.
        LOG.info("Fetching regularisation prompt");
        return metaData.get("RequestPrompt");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInput() {
        // this function returns the input.
        LOG.info("Fetching input json string containing points.");
        return metaData.get("InputData");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReqType() {
        // this returns "REG" as this holds
        // the regularization request
        LOG.info("Fetching Request type -- regularisation");
        return type;
    }
}
