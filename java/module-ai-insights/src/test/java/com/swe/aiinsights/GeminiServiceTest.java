/*
 * -----------------------------------------------------------------------------
 *  File: GeminiServiceTest.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

/*
 * References :
 * https://www.baeldung.com/java-reflection,
 * https://medium.com/@AlexanderObregon/how-to-test-private-methods-in-java-ec1872e81911
 */

package com.swe.aiinsights;

import com.swe.aiinsights.aiservice.GeminiService;
import com.swe.aiinsights.customexceptions.RateLimitException;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.getkeys.GeminiKeyManager;
import com.swe.aiinsights.modeladapter.GeminiAdapter;
import com.swe.aiinsights.response.AiResponse;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

/**
 * Test class for GeminiService.
 */
@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {
    /**
     * Mock http client.
     */
    @Mock
    private OkHttpClient mockHttpClient;

    /**
     * Mock http call.
     */
    @Mock
    private Call mockCall;

    /**
     * Mock response.
     */
    @Mock
    private Response mockResponse;

    /**
     * Mock request generaliser.
     */
    @Mock
    private RequestGeneraliser mockRequestGeneraliser;

    /**
     * mocked response.
     */
    @Mock
    private AiResponse mockAiResponse;

    /**
     * response body mock.
     */
    @Mock
    private ResponseBody mockResponseBody;

    @Test
    void testRunProcessSuccess() throws Exception {
        final int numKeys = 3;
        try (MockedConstruction<GeminiKeyManager> keyManagerMock = mockConstruction(
                GeminiKeyManager.class,
                (mock, context) -> {
                    when(mock.getNumberOfKeys()).thenReturn(numKeys);
                    when(mock.getCurrentKey()).thenReturn("test-key-1");
                })) {

            try (MockedStatic<Dotenv> dotenvMock = mockStatic(Dotenv.class)) {
                final Dotenv localMockDotenv = mock(Dotenv.class);
                dotenvMock.when(Dotenv::load).thenReturn(localMockDotenv);
                lenient().when(localMockDotenv.get("GEMINI_URL")).thenReturn("https://api.gemini.com/");

                final GeminiService service = new GeminiService();

                final var httpClientField = GeminiService.class.getDeclaredField("httpClient");
                httpClientField.setAccessible(true);
                httpClientField.set(service, mockHttpClient);

                try (MockedConstruction<GeminiAdapter> adapterMock = mockConstruction(
                        GeminiAdapter.class,
                        (mock, context) -> {
                            when(mock.buildRequest(any())).thenReturn("{\"request\":\"body\"}");
                            when(mock.getResponse(any())).thenReturn("AI response text");
                        })) {

                    when(mockRequestGeneraliser.getAiResponse()).thenReturn(mockAiResponse);
                    when(mockHttpClient.newCall(any())).thenReturn(mockCall);
                    when(mockCall.execute()).thenReturn(mockResponse);
                    when(mockResponse.isSuccessful()).thenReturn(true);

                    final AiResponse result = service.runProcess(mockRequestGeneraliser);

                    assertNotNull(result);
                    verify(mockAiResponse).setResponse("AI response text");
                }
            }
        }
    }

    @Test
    void testRunProcessRateLimitHitSwitchesKey() throws Exception {
        final int numKeys = 2;
        try (MockedConstruction<GeminiKeyManager> keyManagerMock = mockConstruction(
                GeminiKeyManager.class,
                (mock, context) -> {
                    when(mock.getNumberOfKeys()).thenReturn(numKeys);
                    when(mock.getCurrentKey())
                            .thenReturn("key1")
                            .thenReturn("key2");
                })) {

            try (MockedStatic<Dotenv> dotenvMock = mockStatic(Dotenv.class)) {
                final Dotenv localMockDotenv = mock(Dotenv.class);
                dotenvMock.when(Dotenv::load).thenReturn(localMockDotenv);
                lenient().when(localMockDotenv.get("GEMINI_URL")).thenReturn("https://api.gemini.com/");

                final GeminiService service = new GeminiService();

                final var httpClientField = GeminiService.class.getDeclaredField("httpClient");
                httpClientField.setAccessible(true);
                httpClientField.set(service, mockHttpClient);

                try (MockedConstruction<GeminiAdapter> adapterMock = mockConstruction(
                        GeminiAdapter.class,
                        (mock, context) -> {
                            when(mock.buildRequest(any())).thenReturn("{\"request\":\"body\"}");
                            when(mock.getResponse(any())).thenReturn("Success response");
                        })) {

                    when(mockRequestGeneraliser.getAiResponse()).thenReturn(mockAiResponse);
                    when(mockHttpClient.newCall(any())).thenReturn(mockCall);

                    final int rateLimitCode = 429;
                    final Response rateLimitResponse = mock(Response.class);
                    when(rateLimitResponse.isSuccessful()).thenReturn(false);
                    when(rateLimitResponse.code()).thenReturn(rateLimitCode);

                    when(mockCall.execute())
                            .thenReturn(rateLimitResponse)
                            .thenReturn(mockResponse);
                    when(mockResponse.isSuccessful()).thenReturn(true);

                    final AiResponse result = service.runProcess(mockRequestGeneraliser);

                    assertNotNull(result);

                    final GeminiKeyManager keyManager = keyManagerMock.constructed().get(0);
                    verify(keyManager).setKeyIndex("key1");
                    verify(mockCall, times(2)).execute();
                }
            }
        }
    }

    @Test
    void testRunProcessNonRateLimitErrorThrowsException() throws Exception {
        try (MockedConstruction<GeminiKeyManager> keyManagerMock = mockConstruction(
                GeminiKeyManager.class,
                (mock, context) -> {
                    when(mock.getNumberOfKeys()).thenReturn(1);
                    when(mock.getCurrentKey()).thenReturn("key1");
                })) {

            try (MockedStatic<Dotenv> dotenvMock = mockStatic(Dotenv.class)) {
                final Dotenv localMockDotenv = mock(Dotenv.class);
                dotenvMock.when(Dotenv::load).thenReturn(localMockDotenv);
                lenient().when(localMockDotenv.get("GEMINI_URL")).thenReturn(
                        "https://api.gemini.com/");

                final GeminiService service = new GeminiService();

                final var httpClientField = GeminiService.class.getDeclaredField("httpClient");
                httpClientField.setAccessible(true);
                httpClientField.set(service, mockHttpClient);

                try (MockedConstruction<GeminiAdapter> adapterMock = mockConstruction(
                        GeminiAdapter.class,
                        (mock, context) -> {
                            lenient().when(mock.buildRequest(any())).thenReturn(
                                    "{\"request\":\"body\"}");
                        })) {

                    lenient().when(mockRequestGeneraliser.getAiResponse()).thenReturn(mockAiResponse);
                    when(mockHttpClient.newCall(any())).thenReturn(mockCall);

                    final int successCode = 500;
                    final Response errorResponse = mock(Response.class);
                    when(errorResponse.isSuccessful()).thenReturn(false);
                    when(errorResponse.code()).thenReturn(successCode);

                    when(mockCall.execute()).thenReturn(errorResponse);

                    assertThrows(RateLimitException.class, () -> {
                        service.runProcess(mockRequestGeneraliser);
                    });
                }
            }
        }
    }


}