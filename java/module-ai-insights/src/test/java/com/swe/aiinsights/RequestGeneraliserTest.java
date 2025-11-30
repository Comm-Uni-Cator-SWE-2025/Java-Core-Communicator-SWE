/*
 * -----------------------------------------------------------------------------
 *  File: RequestGeneraliserTest.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201003
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.aiinsights.data.WhiteBoardData;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.request.AiDescriptionRequest;
import com.swe.aiinsights.request.AiRegularisationRequest;
import com.swe.aiinsights.request.AiSummarisationRequest;
import com.swe.aiinsights.request.AiInsightsRequest;
import com.swe.aiinsights.request.AiActionItemsRequest;
import com.swe.aiinsights.request.AiQuestionAnswerRequest;
import com.swe.aiinsights.response.AiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Complete test suite for all RequestGeneraliser.
 */
@ExtendWith(MockitoExtension.class)
class RequestGeneraliserTest {

    /**
     * whiteboard data mocked.
     */
    @Mock
    private WhiteBoardData mockWhiteBoardData;

    /**
     * mocked regularisation request.
     */
    @Mock
    private AiRegularisationRequest regularisationRequest;

    /**
     * Mocked summarisationRequest.
     */
    @Mock
    private AiSummarisationRequest aiSummarisationRequest;

    /**
     * Mocked insights request.
     */
    @Mock
    private AiInsightsRequest aiInsightsRequest;

    /**
     * Mocked action items request.
     */
    @Mock
    private AiActionItemsRequest aiActionItemsRequest;

    /**
     * AiQuestionAnswer test - mocked.
     */
    @Mock
    private AiQuestionAnswerRequest aiQuestionAnswerRequest;

    /**
     * Description request mocked.
     */
    @Mock
    private AiDescriptionRequest aiDescriptionRequest;

    /**
     * mockedd json node.
     */
    @Mock
    private JsonNode jsonChat;

    /**
     * object mapper to create json.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void testAiDescriptionRequestGeneralisation() throws IOException {
        // Arrange
        when(aiDescriptionRequest.getInput()).thenReturn("image_base64_data");
        when(aiDescriptionRequest.getContext()).thenReturn("describe");
        when(aiDescriptionRequest.getReqType()).thenReturn("DESC");


        final RequestGeneraliser req = new RequestGeneraliser(aiDescriptionRequest);

        assertNotNull(req);
        assertEquals("DESC", req.getReqType());
        assertEquals("image_base64_data", req.getImgData());
        assertEquals("describe", req.getPrompt());
        assertNull(req.getTextData());
    }

    @Test
    void testAiRegularisationRequestGeneralisation() throws IOException {
        // Arrange
        when(regularisationRequest.getInput()).thenReturn("points list");
        when(regularisationRequest.getContext()).thenReturn("regularise");
        when(regularisationRequest.getReqType()).thenReturn("REG");


        final RequestGeneraliser req = new RequestGeneraliser(regularisationRequest);

        assertNotNull(req);
        assertEquals("REG", req.getReqType());
        assertNull(req.getImgData());
        assertEquals("regularise", req.getPrompt());
        assertEquals("points list", req.getTextData());
    }

    @Test
    void testAiSummarisationRequestGeneralisation() throws IOException {
        // Arrange
        when(aiSummarisationRequest.getInput()).thenReturn("chat data");
        when(aiSummarisationRequest.getContext()).thenReturn("summarise");
        when(aiSummarisationRequest.getReqType()).thenReturn("SUM");


        final RequestGeneraliser req = new RequestGeneraliser(aiSummarisationRequest);

        assertNotNull(req);
        assertEquals("SUM", req.getReqType());
        assertNull(req.getImgData());
        assertEquals("summarise", req.getPrompt());
        assertEquals("chat data", req.getTextData());
    }


    @Test
    void testAiInsightsRequestGeneralisation() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree("{\"chat data\":\"hi\"}");
        // Arrange
        when(aiInsightsRequest.getInput()).thenReturn(node);
        when(aiInsightsRequest.getContext()).thenReturn("insights");
        when(aiInsightsRequest.getReqType()).thenReturn("INS");


        final RequestGeneraliser req = new RequestGeneraliser(aiInsightsRequest);

        assertNotNull(req);
        assertEquals("INS", req.getReqType());
        assertNull(req.getImgData());
        assertEquals("insights", req.getPrompt());
        assertEquals("{\"chat data\":\"hi\"}", req.getTextData());
    }

    @Test
    void testAiQnARequestGeneralisation() throws IOException {
        when(aiQuestionAnswerRequest.getInput()).thenReturn("chat data");
        when(aiQuestionAnswerRequest.getContext()).thenReturn("question");
        when(aiQuestionAnswerRequest.getReqType()).thenReturn("QNA");


        final RequestGeneraliser req = new RequestGeneraliser(aiQuestionAnswerRequest);

        assertNotNull(req);
        assertEquals("QNA", req.getReqType());
        assertNull(req.getImgData());
        assertEquals("question", req.getPrompt());
        assertEquals("chat data", req.getTextData());
    }

    @Test
    void testAiActionItemsRequestGeneralisation() throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree("{\"chat data\":\"hi\"}");
        when(aiActionItemsRequest.getInput()).thenReturn(node);
        when(aiActionItemsRequest.getContext()).thenReturn("action items");
        when(aiActionItemsRequest.getReqType()).thenReturn("ACTION");


        final RequestGeneraliser req = new RequestGeneraliser(aiActionItemsRequest);

        assertNotNull(req);
        assertEquals("ACTION", req.getReqType());
        assertNull(req.getImgData());
        assertEquals("action items", req.getPrompt());
        assertEquals("{\"chat data\":\"hi\"}", req.getTextData());
    }

    @Test
    void testAiResponse() throws IOException {
        when(aiDescriptionRequest.getInput()).thenReturn("image_base64_data");
        when(aiDescriptionRequest.getContext()).thenReturn("describe");
        when(aiDescriptionRequest.getReqType()).thenReturn("DESC");


        final RequestGeneraliser req = new RequestGeneraliser(aiDescriptionRequest);

        final AiResponse response = req.getAiResponse();
        final String format = req.formatOutput(response);
        assertNotNull(response);
    }




    @Test
    void testFormatRegularise() throws IOException {
        when(regularisationRequest.getInput()).thenReturn("""
                {
                  "ShapeId": "c585b84a",
                  "Type": "FREEHAND",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 2,
                  "CreatedBy": "user_default",
                  "LastModifiedBy": "user_default",
                  "IsDeleted": false
                }""");
        when(regularisationRequest.getContext()).thenReturn("regularise");
        when(regularisationRequest.getReqType()).thenReturn("REG");


        final RequestGeneraliser req = new RequestGeneraliser(regularisationRequest);

        final AiResponse response = req.getAiResponse();
        response.setResponse("""
                {
                  "ShapeId": "c585b84a",
                  "Type": "FREEHAND",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 2,
                  "CreatedBy": "user_default",
                  "LastModifiedBy": "user_default",
                  "IsDeleted": false
                }""");
        final String output = req.formatOutput(response);
        assertNotNull(output);
    }

    @Test
    void testFormatInsights() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree("{\"chat data\":\"hi\"}");
        when(aiInsightsRequest.getInput()).thenReturn(node);
        when(aiInsightsRequest.getContext()).thenReturn("insights");
        when(aiInsightsRequest.getReqType()).thenReturn("INS");


        final RequestGeneraliser req = new RequestGeneraliser(aiInsightsRequest);

        final AiResponse response = req.getAiResponse();
        response.setResponse("""
                [
                  {
                    "time": "2025-11-07T10:00:00Z",
                    "sentiment": 7.0
                  },
                  {
                    "time": "2025-11-07T10:01:45Z",
                    "sentiment": 3.0
                  },
                  {
                    "time": "2025-11-07T10:03:20Z",
                    "sentiment": -3.0
                  },
                  {
                    "time": "2025-11-07T10:04:50Z",
                    "sentiment": 2.0
                  },
                  {
                    "time": "2025-11-07T10:06:10Z",
                    "sentiment": 6.0
                  },
                  {
                    "time": "2025-11-07T10:07:30Z",
                    "sentiment": 4.0
                  },
                  {
                    "time": "2025-11-07T10:08:55Z",
                    "sentiment": 7.0
                  },
                  {
                    "time": "2025-11-07T10:10:22Z",
                    "sentiment": 8.0
                  },
                  {
                    "time": "2025-11-07T10:12:40Z",
                    "sentiment": -2.0
                  },
                  {
                    "time": "2025-11-07T10:14:00Z",
                    "sentiment": 3.0
                  }
                ]""");
        final String output = req.formatOutput(response);
        assertNotNull(output);
    }

    @Test
    void testDefault() throws IOException {
        when(regularisationRequest.getReqType()).thenReturn("DEFAULT");
        when(regularisationRequest.getInput()).thenReturn("dummy input");
        when(regularisationRequest.getContext()).thenReturn("dummy prompt");

        final RequestGeneraliser reg = new RequestGeneraliser(regularisationRequest);

        assertNull(reg.getAiResponse());
    }





}