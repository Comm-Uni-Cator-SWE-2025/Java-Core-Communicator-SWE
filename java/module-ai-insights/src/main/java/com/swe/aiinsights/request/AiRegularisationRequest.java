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
    private String type = "REG";
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
        metaData.put("InputData", points);
        metaData.put("RequestPrompt",
                "You are given a list of 2D points representing a freehand drawing.\n" +
                        "\n" +
                        "Your tasks:\n" +
                        "1. Use ONLY the provided points.\n" +
                        "2. Identify the geometric shape that best matches those points.\n" +
                        "   Allowed values (case-sensitive): Ellipse, Square, Triangle, Rectangle, StraightLine.\n" +
                        "3. Compute the axis-aligned bounding box of that shape.\n" +
                        "4. Output EXACTLY TWO points:\n" +
                        "   - First: the top-left coordinate\n" +
                        "   - Second: the bottom-right coordinate\n" +
                        "\n" +
                        "You MUST output one JSON object ONLY.\n" +
                        "You MUST use the EXACT schema below.\n" +
                        "You MUST NOT add extra fields, remove fields, reorder fields, or rename fields.\n" +
                        "You MUST NOT output more than two points under \"Points\".  \n" +
                        "You MUST NOT invent additional points.\n" +
                        "\n" +
                        "Return ONLY a JSON object with the EXACT structure below:\n" +
                        "\n" +
                        "{\n" +
                        "  \"ShapeId\": \"<ShapeId>\",\n" +
                        "  \"type\": \"<OneOf: Ellipse | Square | Triangle | Rectangle | StraightLine>\",\n" +
                        "  \"Points\": [\n" +
                        "    { \"x\": <number>, \"y\": <number> },\n" +
                        "    { \"x\": <number>, \"y\": <number> }\n" +
                        "  ],\n" +
                        "  \"Color\": \"<Color>\",\n" +
                        "  \"Thickness\": \"<Thickness>\",\n" +
                        "  \"CreatedBy\": \"<CreatedBy>\",\n" +
                        "  \"LastModifiedBy\": \"<LastModifiedBy>\",\n" +
                        "  \"IsDeleted\": <true|false>\n" +
                        "}\n" +
                        "\n" +
                        "STRICT RULES:\n" +
                        "- \"Points\" must contain EXACTLY 2 elements. No more, no less.\n" +
                        "- Each element must contain ONLY {\"x\": <number>, \"y\": <number>}.\n" +
                        "- Do not output 3 or 4 points. Do not output bounding box corners separately.\n" +
                        "The following metadata fields MUST be copied EXACTLY as provided in the input:\n" +
                        "- \"ShapeId\"\n" +
                        "- \"Color\"\n" +
                        "- \"Thickness\"\n" +
                        "- \"CreatedBy\"\n" +
                        "- \"LastModifiedBy\"\n" +
                        "- \"IsDeleted\"\n" +
                        "\n" +
                        "You MUST NOT modify, rename, reinterpret, normalize, guess, or replace the values of these fields.\n" +
                        "You MUST copy the exact input strings and numbers verbatim into the output JSON.\n" +
                        "- Output must be valid JSON. No explanations. No additional text.\n" +
                        "\n" +
                        "Use only the points provided below.\n"
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
        type = "REG";
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
