/*
 * -----------------------------------------------------------------------------
 *  File: AsyncAiExecutorTest.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.swe.aiinsights.aiservice.LlmService;
import com.swe.aiinsights.apiendpoints.AsyncAiExecutor;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.response.AiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


/**
 * Complete test class for AsyncAiExecutor with 100% coverage.
 */
@ExtendWith(MockitoExtension.class)
class AsyncAiExecutorTest {
    /**
     * AsyncAiExecutor to test.
     */
    private AsyncAiExecutor asyncAiExecutor;

    /**
     * Mocked request to send.
     */
    @Mock
    private AiRequestable mockRequest;

    /**
     * mocked llm service.
     */
    @Mock
    private LlmService mockLlmService;

    /**
     * mocked AiResponse.
     */
    @Mock
    private AiResponse mockAiResponse;

    @BeforeEach
    void setUp() {
        asyncAiExecutor = new AsyncAiExecutor();
    }


    @Test
    void testExecuteSuccessPathReturnsFormattedResponse() throws Exception {
        when(mockRequest.getReqType()).thenReturn("SUM");
        when(mockRequest.getContext()).thenReturn("test prompt");
        when(mockRequest.getInput()).thenReturn("test data");
        when(mockAiResponse.getResponse()).thenReturn("AI raw response");

        final Field llmServiceField = AsyncAiExecutor.class.getDeclaredField("llmService");
        llmServiceField.setAccessible(true);
        llmServiceField.set(asyncAiExecutor, mockLlmService);

        when(mockLlmService.runProcess(any(RequestGeneraliser.class)))
                .thenReturn(mockAiResponse);

        final CompletableFuture<String> result = asyncAiExecutor.execute(mockRequest);

        assertNotNull(result);
        final String response = result.get(); // This will cover the return response line
        assertEquals("AI raw response", response);
        verify(mockLlmService).runProcess(any(RequestGeneraliser.class));
    }


    @Test
    void testExecuteIOExceptionInRunProcess() throws Exception {
        when(mockRequest.getReqType()).thenReturn("DESC");
        when(mockRequest.getContext()).thenReturn("test");
        when(mockRequest.getInput()).thenReturn("data");

        final Field llmServiceField = AsyncAiExecutor.class.getDeclaredField("llmService");
        llmServiceField.setAccessible(true);
        llmServiceField.set(asyncAiExecutor, mockLlmService);

        when(mockLlmService.runProcess(any(RequestGeneraliser.class)))
                .thenThrow(new IOException("Service failed"));

        final CompletableFuture<String> result = asyncAiExecutor.execute(mockRequest);

        final ExecutionException exception = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertInstanceOf(IOException.class, exception.getCause().getCause());
    }

    @Test
    void testExecuteUnexpectedExceptionInRunProcess() throws Exception {
        // Arrange
        when(mockRequest.getReqType()).thenReturn("QNA");
        when(mockRequest.getContext()).thenReturn("test");
        when(mockRequest.getInput()).thenReturn("data");

        final Field llmServiceField = AsyncAiExecutor.class.getDeclaredField("llmService");
        llmServiceField.setAccessible(true);
        llmServiceField.set(asyncAiExecutor, mockLlmService);

        when(mockLlmService.runProcess(any(RequestGeneraliser.class)))
                .thenThrow(new NullPointerException("Null error"));

        final CompletableFuture<String> result = asyncAiExecutor.execute(mockRequest);


        final ExecutionException exception = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }

}