/*
 * -----------------------------------------------------------------------------
 *  File: OllamaAdapterTest.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.data
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.modeladapter.OllamaAdapter;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

/**
 * Test class for OllamaAdapter.
 */
@ExtendWith(MockitoExtension.class)
class OllamaAdapterTest {
    /**
     * ollama adapter to test.
     */
    private OllamaAdapter adapter;

    /**
     * Mock request generaliser.
     */
    @Mock
    private RequestGeneraliser mockRequest;

    /**
     * Mock response.
     */
    @Mock
    private Response mockResponse;

    /**
     * Mock response body - http.
     */
    @Mock
    private ResponseBody mockResponseBody;

    @BeforeEach
    void setUp() {
        adapter = new OllamaAdapter();
    }


    @Test
    void testBuildRequestWithTextData() throws JsonProcessingException {
        when(mockRequest.getPrompt()).thenReturn("Summarize: ");
        when(mockRequest.getTextData()).thenReturn("Text to process");
        when(mockRequest.getImgData()).thenReturn(null);

        final String result = adapter.buildRequest(mockRequest);

        assertNotNull(result);
        verify(mockRequest).getPrompt();
        verify(mockRequest, atLeastOnce()).getTextData(); // CHANGE THIS
        verify(mockRequest).getImgData();
    }

    @Test
    void testBuildRequestWithImageData() throws JsonProcessingException {
        when(mockRequest.getPrompt()).thenReturn("Describe image: ");
        when(mockRequest.getTextData()).thenReturn("Some text");
        when(mockRequest.getImgData()).thenReturn("base64ImageData");

        final String result = adapter.buildRequest(mockRequest);

        assertNotNull(result);
        verify(mockRequest).getPrompt();
        verify(mockRequest).getTextData();
        verify(mockRequest, atLeastOnce()).getImgData(); // CHANGE THIS
    }

    @Test
    void testBuildRequestContainsCorrectOptions() throws JsonProcessingException {
        when(mockRequest.getPrompt()).thenReturn("Test");
        when(mockRequest.getTextData()).thenReturn("Data");
        when(mockRequest.getImgData()).thenReturn(null);

        final String result = adapter.buildRequest(mockRequest);

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.readTree(result);

        // Verify options
        assertTrue(jsonNode.has("options"));
        final JsonNode options = jsonNode.get("options");

        final double topp = 0.9;
        final double temp = 0.2;
        final int numctx = 16384;

        final double delta = 0.001;
        assertTrue(result.contains("prompt"));
        assertFalse(jsonNode.get("stream").asBoolean());
        assertEquals("gemma3", jsonNode.get("model").asText());
        assertEquals(numctx, options.get("num_ctx").asInt());
        assertEquals(temp, options.get("temperature").asDouble(), delta);
        assertEquals(topp, options.get("top_p").asDouble(), delta);
    }


    @Test
    void testGetResponseSuccess() throws IOException {
        final String jsonResponse = """
            {
              "response": "This is the Ollama response"
            }
            """;

        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.charStream()).thenReturn(new StringReader(jsonResponse));

        final String result = adapter.getResponse(mockResponse);

        assertEquals("This is the Ollama response", result);
        verify(mockResponse, times(2)).body();
    }


    @Test
    void testGetResponseResponseFieldIsNotTextual() throws IOException {
        final String jsonResponse = """
            {
              "response": 12345
            }
            """;

        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.charStream()).thenReturn(new StringReader(jsonResponse));

        final IOException exception = assertThrows(IOException.class, () -> {
            adapter.getResponse(mockResponse);
        });

        assertTrue(exception.getMessage().contains("Invalid Ollama response"));
    }

    @Test
    void testGetResponseInvalidJson() throws IOException {
        final String jsonResponse = "{ invalid json }";

        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.charStream()).thenReturn(new StringReader(jsonResponse));

        assertThrows(IOException.class, () -> {
            adapter.getResponse(mockResponse);
        });
    }

}