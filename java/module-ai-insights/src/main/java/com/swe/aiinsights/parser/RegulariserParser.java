/**
 * Parses, validates, and post-processes AI-generated regularisation output.
 *
 * <p>
 * This parser cleans AI output, validates JSON structure, recovers from
 * malformed responses, and merges the corrected data with original metadata.
 * It ensures fallback behaviour when the AI response is incomplete or invalid.
 * </p>
 *
 * @author Abhirami R Iyer
 */

package com.swe.aiinsights.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This parser cleans AI output, validates JSON structure.
 */
public class RegulariserParser {

    /**
     * Cleans and validates the AI response.
     * merging corrected points with the original shape metadata.
     * In case of improper/incomplete
     * response fallback to input request
     *
     * @param inputJsonString the original input JSON.
     * @param aiResponse      the raw AI response.
     * @return cleaned and validated regularised JSON.
     * @throws JsonProcessingException if JSON parsing fails.
     */
    public String parseInput(final String inputJsonString, final String aiResponse)
            throws JsonProcessingException {

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode input = mapper.readTree(inputJsonString);

        final String shapeId = input.get("ShapeId").asText();
        final String color = input.get("Color").asText();
        final int thickness = input.get("Thickness").asInt();
        final String createdBy = input.get("CreatedBy").asText();
        final String lastModifiedBy = input.get("LastModifiedBy").asText();
        final boolean isDeleted = input.get("IsDeleted").asBoolean();

        final String cleanedAI = cleanResponse(aiResponse);

        final JsonNode aiNode = parseJsonSafely(mapper, cleanedAI);
        if (aiNode == null || !aiNode.has("Points")) {
            return inputJsonString;
        }

        final String newType = extractType(aiNode, input);

        final ArrayNode aiPoints = (ArrayNode) aiNode.get("Points");
        if (aiPoints == null || aiPoints.isEmpty()) {
            return inputJsonString;
        }

        final ArrayNode finalPoints = buildFinalPoints(mapper, aiPoints);
        final ObjectNode finalNode = mapper.createObjectNode();
        finalNode.put("ShapeId", shapeId);
        finalNode.put("Type", newType);
        finalNode.set("Points", finalPoints);
        finalNode.put("Color", color);
        finalNode.put("Thickness", thickness);
        finalNode.put("CreatedBy", createdBy);
        finalNode.put("LastModifiedBy", lastModifiedBy);
        finalNode.put("IsDeleted", isDeleted);

        return mapper.writeValueAsString(finalNode);
    }

    /**
     * Removes markdown fences and trims whitespace.
     *
     * @param aiResponse raw AI response.
     * @return cleaned JSON-like string.
     */
    private String cleanResponse(final String aiResponse) {
        if (aiResponse == null) {
            return "";
        }

        String cleaned = aiResponse.trim();

        if (!cleaned.contains("{")) {
            return "";
        }

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replace("```json", "")
                    .replace("```", "")
                    .trim();
        }

        return cleaned;
    }

    /**
     * Parses AI response JSON safely without throwing.
     *
     * @param mapper    the ObjectMapper instance.
     * @param cleanedAI cleaned AI JSON.
     * @return parsed JsonNode or null on failure.
     */
    private JsonNode parseJsonSafely(final ObjectMapper mapper, final String cleanedAI) {
        try {
            final JsonNode aiNode = mapper.readTree(cleanedAI);
            if (aiNode.isObject()) {
                return aiNode;
            } else {
                return null;
            }

        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Extracts shape type from AI response or falls back to original metadata.
     *
     * @param aiNode AI JSON node.
     * @param input  original input JSON.
     * @return resolved type value.
     */
    private String extractType(final JsonNode aiNode, final JsonNode input) {
        if (aiNode.has("type")) {
            return aiNode.get("type").asText();
        }
        if (aiNode.has("Type")) {
            return aiNode.get("Type").asText();
        }
        return input.path("Type").asText();
    }

    /**
     * Ensures that at least two points exist.
     *
     * @param mapper   mapper instance.
     * @param aiPoints AI points array.
     * @return a normalised array of 2 points.
     */
    private ArrayNode buildFinalPoints(final ObjectMapper mapper, final ArrayNode aiPoints) {
        final ArrayNode finalPoints = mapper.createArrayNode();

        if (aiPoints.size() >= 2) {
            finalPoints.add(aiPoints.get(0));
            finalPoints.add(aiPoints.get(1));
        } else {
            finalPoints.add(aiPoints.get(0));
            finalPoints.add(aiPoints.get(0));
        }

        return finalPoints;
    }


}
