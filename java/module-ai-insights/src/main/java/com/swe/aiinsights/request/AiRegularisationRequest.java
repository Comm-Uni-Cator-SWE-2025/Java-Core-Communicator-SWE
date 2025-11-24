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
        "You are given a list of 2D points representing a freehand drawing.
       \s
         Your tasks:
        1. Use ONLY the provided points.
        2. Identify the geometric shape that best matches those points.
           Allowed values (case-sensitive): ELLIPSE, TRIANGLE, RECTANGLE, LINE.
        3. Compute the axis-aligned bounding box of that shape.
        4. Output EXACTLY TWO points:
           - First: the top-left coordinate
           - Second: the bottom-right coordinate
       \s
        You MUST output one JSON object ONLY.
        You MUST use the EXACT schema below.
        You MUST NOT add extra fields, remove fields, reorder fields, or rename fields.
        You MUST NOT output more than two points under "Points". \s
        You MUST NOT invent additional points.
       \s
        Return ONLY a JSON object with the EXACT structure below:
       \s
        {
          "ShapeId": "<ShapeId>",
          "type": "<OneOf: ELLIPSE | TRIANGLE | RECTANGLE | LINE>",
          "Points": [
            { "X": <number>, "Y": <number> },
            { "X": <number>, "Y": <number> }
          ],
          "Color": "<Color>",
          "Thickness": "<Thickness>",
          "CreatedBy": "<CreatedBy>",
          "LastModifiedBy": "<LastModifiedBy>",
          "IsDeleted": <true|false>
        }
       \s
        STRICT RULES:
        - "Points" must contain EXACTLY 2 elements. No more, no less.
        - Each element must contain ONLY {"x": <number>, "y": <number>}.
        - Do not output 3 or 4 points. Do not output bounding box corners separately.
        The following metadata fields MUST be copied EXACTLY as provided in the input:
        - "ShapeId"
        - "Color"
        - "Thickness"
        - "CreatedBy"
        - "LastModifiedBy"
        - "IsDeleted"
       \s
        You MUST NOT modify, rename, reinterpret, normalize, guess, or replace the values of these fields.
        You MUST copy the exact input strings and numbers verbatim into the output JSON.
        - Output must be valid JSON. No explanations. No additional text.
       \s
        Use only the points provided below.\s""");

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
