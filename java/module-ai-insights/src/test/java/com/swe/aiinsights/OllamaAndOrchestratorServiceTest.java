/*
 * -----------------------------------------------------------------------------
 *  File: OllamaAndOrchestratorServiceTest.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.swe.aiinsights.aiservice.LlmOrchestratorService;
import com.swe.aiinsights.aiservice.LlmService;
import com.swe.aiinsights.aiservice.OllamaService;
import com.swe.aiinsights.customexceptions.RateLimitException;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.modeladapter.OllamaAdapter;
import com.swe.aiinsights.response.AiResponse;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * Tests for OllamaService and LlmOrchestratorService.
 */
@ExtendWith(MockitoExtension.class)
class OllamaAndOrchestratorServiceTest {

    /**
     * mock dotenv object.
     */
    @Mock
    private Dotenv mockDotenv;

    /**
     * mock http client.
     */
    @Mock
    private OkHttpClient mockHttpClient;

    /**
     * mock calling object.
     */
    @Mock
    private Call mockCall;

    /**
     * mockResponse.
     */
    @Mock
    private Response mockResponse;

    /**
     * mockResponseBody.
     */
    @Mock
    private ResponseBody mockResponseBody;

    /**
     * mock RequestGeneralilser.
     */
    @Mock
    private RequestGeneraliser mockRequestGeneraliser;

    /**
     * mockAiResponse.
     */
    @Mock
    private AiResponse mockAiResponse;

    /**
     * mock LLmService.
     */
    @Mock
    private LlmService mockLlmService1;

    /**
     * mock Llmservice.
     */
    @Mock
    private LlmService mockLlmService2;


    // test constructor of Ollama service
    @Test
    void testOllamaServiceConstructor() {
        try (MockedStatic<Dotenv> dotenvMock = mockStatic(Dotenv.class)) {
            dotenvMock.when(Dotenv::load).thenReturn(mockDotenv);
            lenient().when(mockDotenv.get("OLLAMA_URL")).thenReturn("http://localhost:11434/api/generate");

            final OllamaService service = new OllamaService();
            assertNotNull(service);
        }
    }

    //test successful execution of run process in Ollama Service
    @Test
    void testOllamaServiceRunProcessSuccess() throws Exception {
        try (MockedStatic<Dotenv> dotenvMock = mockStatic(Dotenv.class)) {
            dotenvMock.when(Dotenv::load).thenReturn(mockDotenv);
            lenient().when(mockDotenv.get("OLLAMA_URL")).thenReturn("http://localhost:11434/api/generate");

            final OllamaService service = new OllamaService();

            final var httpClientField = OllamaService.class.getDeclaredField("httpClient");
            httpClientField.setAccessible(true);
            httpClientField.set(service, mockHttpClient);

            try (MockedConstruction<OllamaAdapter> adapterMock = mockConstruction(
                    OllamaAdapter.class,
                    (mock, context) -> {
                        when(mock.buildRequest(any())).thenReturn("{\"model\":\"gemma3\",\"prompt\":\"test\"}");
                        when(mock.getResponse(any())).thenReturn("Ollama response");
                    })) {

                when(mockRequestGeneraliser.getAiResponse()).thenReturn(mockAiResponse);
                when(mockHttpClient.newCall(any())).thenReturn(mockCall);
                when(mockCall.execute()).thenReturn(mockResponse);
                when(mockResponse.isSuccessful()).thenReturn(true);
                final int success = 200;
                when(mockResponse.code()).thenReturn(success);
                final AiResponse result = service.runProcess(mockRequestGeneraliser);

                assertNotNull(result);
                verify(mockAiResponse).setResponse("Ollama response");
            }
        }
    }

    // test failure of Ollama service run process
    @Test
    void testOllamaServiceRunProcessFailure() throws Exception {
        try (MockedStatic<Dotenv> dotenvMock = mockStatic(Dotenv.class)) {
            dotenvMock.when(Dotenv::load).thenReturn(mockDotenv);
            lenient().when(mockDotenv.get("OLLAMA_URL")).thenReturn("http://localhost:11434/api/generate");

            final OllamaService service = new OllamaService();

            final var httpClientField = OllamaService.class.getDeclaredField("httpClient");
            httpClientField.setAccessible(true);
            httpClientField.set(service, mockHttpClient);

            try (MockedConstruction<OllamaAdapter> adapterMock = mockConstruction(
                    OllamaAdapter.class,
                    (mock, context) -> {
                        when(mock.buildRequest(any())).thenReturn("{\"model\":\"llama2\"}");
                    })) {

                when(mockRequestGeneraliser.getAiResponse()).thenReturn(mockAiResponse);
                when(mockHttpClient.newCall(any())).thenReturn(mockCall);
                when(mockCall.execute()).thenReturn(mockResponse);
                when(mockResponse.isSuccessful()).thenReturn(false);
                final int error = 500;
                when(mockResponse.code()).thenReturn(error);
                when(mockResponse.body()).thenReturn(mockResponseBody);
                when(mockResponseBody.string()).thenReturn("Internal server error");

                assertThrows(IOException.class, () -> {
                    service.runProcess(mockRequestGeneraliser);
                });
            }
        }
    }

    // LlmOrchestratorService Tests

    //check creation of Orchestrator service
    @Test
    void testOrchestratorConstructorValidServices() {
        final List<LlmService> services = Arrays.asList(mockLlmService1, mockLlmService2);
        final LlmOrchestratorService orchestrator = new LlmOrchestratorService(services);
        assertNotNull(orchestrator);
    }

    // exception handling
    @Test
    void testOrchestratorConstructorNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LlmOrchestratorService(null);
        });
    }

    @Test
    void testOrchestratorConstructorEmptyServices() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LlmOrchestratorService(Collections.emptyList());
        });
    }

    // first service will be successful
    @Test
    void testOrchestratorRunProcessFirstService() throws IOException {
        final List<LlmService> services = Arrays.asList(mockLlmService1, mockLlmService2);
        final LlmOrchestratorService orchestrator = new LlmOrchestratorService(services);
        when(mockLlmService1.runProcess(any())).thenReturn(mockAiResponse);
        final AiResponse result = orchestrator.runProcess(mockRequestGeneraliser);
        assertNotNull(result);
        verify(mockLlmService1).runProcess(mockRequestGeneraliser);
        verify(mockLlmService2, never()).runProcess(any());
    }

    @Test
    void testOrchestratorRunProcessFirstFailSecondSuccess() throws IOException {
        final List<LlmService> services = Arrays.asList(mockLlmService1, mockLlmService2);
        final LlmOrchestratorService orchestrator = new LlmOrchestratorService(services);

        when(mockLlmService1.runProcess(any())).thenThrow(new RateLimitException("Rate limit hit"));
        when(mockLlmService2.runProcess(any())).thenReturn(mockAiResponse);

        final AiResponse result = orchestrator.runProcess(mockRequestGeneraliser);

        assertNotNull(result);
        verify(mockLlmService1).runProcess(mockRequestGeneraliser);
        verify(mockLlmService2).runProcess(mockRequestGeneraliser);
    }

    @Test
    void testOrchestratorRunProcessAllServicesFail() throws IOException {
        final List<LlmService> services = Arrays.asList(mockLlmService1, mockLlmService2);
        final LlmOrchestratorService orchestrator = new LlmOrchestratorService(services);

        when(mockLlmService1.runProcess(any())).thenThrow(new RateLimitException("Rate limit 1"));
        when(mockLlmService2.runProcess(any())).thenThrow(new RateLimitException("Rate limit 2"));

        assertThrows(IOException.class, () -> {
            orchestrator.runProcess(mockRequestGeneraliser);
        });

        verify(mockLlmService1).runProcess(mockRequestGeneraliser);
        verify(mockLlmService2).runProcess(mockRequestGeneraliser);
    }
}