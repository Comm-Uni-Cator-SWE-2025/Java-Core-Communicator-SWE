/*
 * -----------------------------------------------------------------------------
 *  File: GeminiAdapterTest.java
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
import com.swe.aiinsights.modeladapter.GeminiAdapter;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;


/**
 * Test class for GeminiAdapter.
 */
@ExtendWith(MockitoExtension.class)
class GeminiAdapterTest {

    /**
     * adapter to test.
     */
    private GeminiAdapter adapter;

    /**
     * mocked request generaliser.
     */
    @Mock
    private RequestGeneraliser mockRequest;

    /**
     * mocked http response.
     */
    @Mock
    private Response mockResponse;

    /**
     * mocked http response body.
     */
    @Mock
    private ResponseBody mockResponseBody;

    @BeforeEach
    void setUp() {
        adapter = new GeminiAdapter();
    }

    @Test
    void testBuildRequestWithPromptOnly() throws JsonProcessingException {
        when(mockRequest.getPrompt()).thenReturn("Test prompt");
        when(mockRequest.getImgData()).thenReturn(null);
        when(mockRequest.getTextData()).thenReturn(null);

        final String result = adapter.buildRequest(mockRequest);

        assertNotNull(result);
        assertTrue(result.contains("Test prompt"));
        assertTrue(result.contains("contents"));
        assertTrue(result.contains("parts"));
        verify(mockRequest).getPrompt();
        verify(mockRequest).getImgData();
    }

    @Test
    void testBuildRequestWithPromptAndImage() throws JsonProcessingException {
        when(mockRequest.getPrompt()).thenReturn("Describe this image");
        when(mockRequest.getImgData()).thenReturn("base64ImageData");
        lenient().when(mockRequest.getTextData()).thenReturn(null);

        final String result = adapter.buildRequest(mockRequest);

        assertNotNull(result);
        assertTrue(result.contains("Describe this image"));
        assertTrue(result.contains("base64ImageData"));
        assertTrue(result.contains("inlineData"));
        assertTrue(result.contains("image/png"));

        verify(mockRequest).getPrompt();
        verify(mockRequest, atLeastOnce()).getImgData();
    }


    @Test
    void testBuildRequestValidJsonStructure() throws JsonProcessingException {
        when(mockRequest.getPrompt()).thenReturn("Test");
        when(mockRequest.getImgData()).thenReturn(null);
        when(mockRequest.getTextData()).thenReturn("Data");

        final String result = adapter.buildRequest(mockRequest);

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.readTree(result);

        assertNotNull(jsonNode.get("contents"));
        assertTrue(jsonNode.get("contents").isArray());
        assertTrue(jsonNode.get("contents").get(0).has("parts"));
    }


    @Test
    void testGetResponseSuccess() throws IOException {
        final String jsonResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "This is the AI response"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.charStream()).thenReturn(new StringReader(jsonResponse));

        final String result = adapter.getResponse(mockResponse);

        assertEquals("This is the AI response", result);
        verify(mockResponse, times(2)).body();
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



    @Test
    void testGetResponseTextNodeIsNotTextual() throws IOException {
        final String jsonResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": 12345
                      }
                    ]
                  }
                }
              ]
            }
            """;

        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.charStream()).thenReturn(new StringReader(jsonResponse));

        final IOException exception = assertThrows(IOException.class, () -> {
            adapter.getResponse(mockResponse);
        });

        assertTrue(exception.getMessage().contains("No text in api response"));
    }


}