/*
 * -----------------------------------------------------------------------------
 *  File: RequestFactoryTest.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201003
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swe.aiinsights.data.WhiteBoardData;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.request.RequestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Test class for RequestFactory.
 */
class RequestFactoryTest {
    /**
     * request factory for testing.
     */
    private RequestFactory requestFactory;
    /**
     * object mapper for building json.
     */
    private ObjectMapper objectMapper;

    /**
     * to create a temporary png.
     */
    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        requestFactory = new RequestFactory();
        objectMapper = new ObjectMapper();
    }

    /**
     * image interpretation test.
      */
    @Test
    void testGetRequestInterpretWithFilePath() throws IOException {
        final Path testFile = tempDir.resolve("test-image.png");
        Files.write(testFile, "test image data".getBytes());
        final String filePath = testFile.toString();
        final WhiteBoardData image = new WhiteBoardData(filePath);
        final AiRequestable request = requestFactory.getRequest("DESC", image);
        assertNotNull(request);
        assertNotNull(request.getInput());
        assertEquals("DESC", request.getReqType());
        assertNotNull(request.getContext());
        assertTrue(request.getContext().contains("Describe this image"));
    }


    //image regularisation
    @Test
    void testGetRequestRegulariseWithValidPoints() throws IOException {
        final String points = "{\n"
                + "  \"ShapeId\": \"c585b84a-d56c-45b8-a0e1-827ae20a014a\",\n"
                + "  \"Type\": \"FreeHand\",\n"
                + "  \"Points\": [\n"
                + "    {\n"
                + "      \"X\": 450,\n"
                + "      \"Y\": 60\n"
                + "    }\n"
                + "  ],\n"
                + "  \"Color\": \"#FF000000\",\n"
                + "  \"Thickness\": 2,\n"
                + "  \"CreatedBy\": \"user_default\",\n"
                + "  \"LastModifiedBy\": \"user_default\",\n"
                + "  \"IsDeleted\": false\n"
                + "}";

        final AiRequestable request = requestFactory.getRequest("REG", points);
        assertNotNull(request);
        assertNotNull(request.getInput());
        assertEquals("REG", request.getReqType());
        assertNotNull(request.getContext());
    }

    //insights generation
    @Test
    void testGetRequestInsights() throws IOException {
        final JsonNode chatData = objectMapper.readTree("""
                [
                   {
                     "from": "student",
                     "to": "teacher",
                     "timestamp": "2025-11-07T10:00:00Z",
                     "message": "I am really excited about today's class!"
                   }]"""
        );
        final AiRequestable request = requestFactory.getRequest("INS", chatData);
        assertNotNull(request);
        assertNotNull(request.getInput());
        assertEquals("INS", request.getReqType());
        assertNotNull(request.getContext());
    }

    // summarisation request
    @Test
    void testGetRequestSummarise() throws IOException {
        final String contentToSummarise = """
                [
                   {
                     "from": "student",
                     "to": "teacher",
                     "timestamp": "2025-11-07T10:00:00Z",
                     "message": "I am really excited about today's class!"
                   }]""";

        final AiRequestable request = requestFactory.getRequest("SUM", contentToSummarise);
        assertNotNull(request);
        assertNotNull(request.getInput());
        assertEquals("SUM", request.getReqType());
        assertNotNull(request.getContext());
    }

    // summarisation request
    @Test
    void testGetRequestAction() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        final ObjectNode messageNode = mapper.createObjectNode();
        messageNode.put("from", "student");
        messageNode.put("to", "teacher");
        messageNode.put("timestamp", "2025-11-07T10:00:00Z");
        messageNode.put("message", "I am really excited about today's class!");
        final ArrayNode content = mapper.createArrayNode();
        content.add(messageNode);
        final JsonNode jsonContent = content;

        final AiRequestable request = requestFactory.getRequest("ACTION", jsonContent);
        assertNotNull(request);
        assertNotNull(request.getInput());
        assertEquals("ACTION", request.getReqType());
        assertNotNull(request.getContext());
    }

    @Test
    void testGetRequestQna() throws IOException {
        final String question = "What were the main points discussed?";
        final String accumulatedSummary = "The team discussed project timeline and budget";
        final AiRequestable request = requestFactory.getRequest("QNA", question, accumulatedSummary);
        assertNotNull(request);
        assertNotNull(request.getInput());
        assertEquals("QNA", request.getReqType());
        assertNotNull(request.getContext());
    }

    //unknown request
    @Test
    void testGetRequestDefault() throws IOException {
        final String unknownType = "UNKNOWN";
        final AiRequestable request = requestFactory.getRequest(unknownType, "some data");
        assertNull(request);
    }

}