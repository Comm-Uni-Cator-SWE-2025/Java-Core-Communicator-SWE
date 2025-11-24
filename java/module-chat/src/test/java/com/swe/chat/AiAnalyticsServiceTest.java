package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiAnalyticsServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Mock
    private AbstractRPC rpc;

    @Mock
    private Networking network;

    @Mock
    private AiClientService aiClientService;

    private AiAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AiAnalyticsService(rpc, network, aiClientService);
    }

    @AfterEach
    void tearDown() throws Exception {
        ScheduledExecutorService scheduler =
                (ScheduledExecutorService) getFieldValue("scheduler");
        scheduler.shutdownNow();
    }

    @Test
    void addMessageToHistoryPersistsWithoutAiTrigger() throws Exception {
        ChatMessage message = new ChatMessage("msg-1", "user-1", "Alice", "Hello team", null);

        service.addMessageToHistory(message);

        List<ChatMessage> history = getHistory();
        assertEquals(1, history.size());
        assertSame(message, history.get(0));
        verify(aiClientService, never()).summariseText(anyString());
        verify(aiClientService, never()).answerQuestion(anyString());
    }

    @Test
    void addMessageToHistoryTriggersAiFlowForCommand() throws Exception {
        when(aiClientService.summariseText(anyString()))
                .thenReturn(CompletableFuture.completedFuture("summary"));
        when(aiClientService.answerQuestion("status update"))
                .thenReturn(CompletableFuture.completedFuture("Sure thing"));

        ChatMessage message = new ChatMessage("msg-2", "user-2", "Bob", "@AI status update", null);
        service.addMessageToHistory(message);

        verify(aiClientService).summariseText(anyString());
        verify(aiClientService).answerQuestion("status update");

        ArgumentCaptor<byte[]> broadcastCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> rpcCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(network).broadcast(
                broadcastCaptor.capture(), eq(ModuleType.CHAT.ordinal()), eq(0));
        verify(rpc).call(eq("chat:new-message"), rpcCaptor.capture());

        byte[] flaggedPacket = broadcastCaptor.getValue();
        byte[] rpcPayload = rpcCaptor.getValue();
        assertEquals(ChatProtocol.FLAG_TEXT_MESSAGE, flaggedPacket[0]);
        assertArrayEquals(
                rpcPayload, Arrays.copyOfRange(flaggedPacket, 1, flaggedPacket.length));

        ChatMessage aiMessage = ChatMessageSerializer.deserialize(rpcPayload);
        assertEquals("AI_Bot", aiMessage.getSenderDisplayName());
        assertEquals("Sure thing", aiMessage.getContent());

        List<ChatMessage> history = getHistory();
        assertEquals(2, history.size());
    }

    @Test
    void handleAiQuestionIgnoresEmptyPrompt() {
        service.handleAiQuestion("@AI   ");

        verify(aiClientService, never()).answerQuestion(anyString());
    }

    @Test
    void handleAiQuestionBroadcastsFallbackWhenAnswerFails() throws Exception {
        when(aiClientService.answerQuestion("help"))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        service.handleAiQuestion("@AI help");

        ArgumentCaptor<byte[]> rpcCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(rpc).call(eq("chat:new-message"), rpcCaptor.capture());
        ChatMessage fallback = ChatMessageSerializer.deserialize(rpcCaptor.getValue());
        assertEquals("I'm sorry, I couldn't process that request.", fallback.getContent());
        verify(network).broadcast(any(), eq(ModuleType.CHAT.ordinal()), eq(0));
    }

    @Test
    void processIncrementalSummarySkipsWithNoMessages() throws Exception {
        invokeProcessIncrementalSummary();

        verify(aiClientService, never()).summariseText(anyString());
    }

    @Test
    void processIncrementalSummaryGeneratesReplyAwareJson() throws Exception {
        AtomicReference<String> capturedJson = new AtomicReference<>();
        when(aiClientService.summariseText(anyString()))
                .thenAnswer(invocation -> {
                    capturedJson.set(invocation.getArgument(0));
                    return CompletableFuture.completedFuture("summary");
                });

        ChatMessage root =
                new ChatMessage("root-id", "user-1", "Alice", "He said \"Hello\"", null);
        ChatMessage reply =
                new ChatMessage("reply-id", "user-2", "Bob", "Line one\nLine two", "root-id");
        ChatMessage orphan =
                new ChatMessage("orphan-id", "user-3", "Cara", "No parent", "missing");

        service.addMessageToHistory(root);
        service.addMessageToHistory(reply);
        service.addMessageToHistory(orphan);

        invokeProcessIncrementalSummary();

        verify(aiClientService, times(1)).summariseText(anyString());
        String historyJson = capturedJson.get();
        assertNotNull(historyJson);
        assertTrue(historyJson.contains("\"from\": \"Alice\""));
        assertTrue(historyJson.contains("He said \\\"Hello\\\""));
        assertTrue(historyJson.contains("\"to\": \"Alice\""));
        assertTrue(historyJson.contains("Line one Line two"));
        assertTrue(historyJson.contains("\"to\": \"ALL\""));

        Map<String, String> senders = getSenderMap();
        assertEquals("Alice", senders.get("root-id"));
        assertEquals("Bob", senders.get("reply-id"));
        assertEquals("Cara", senders.get("orphan-id"));
    }

    @Test
    void generateChatHistoryJsonHandlesNullFields() throws Exception {
        Method generator =
                AiAnalyticsService.class.getDeclaredMethod(
                        "generateChatHistoryJson", List.class);
        generator.setAccessible(true);

        ChatMessage nullMessage =
                new ChatMessage("n-id", "user", null, null, null);
        @SuppressWarnings("unchecked")
        String json =
                (String) generator.invoke(service, new ArrayList<>(List.of(nullMessage)));

        assertTrue(json.contains("\"from\": \"\""));
        assertTrue(json.contains("\"message\": \"\""));
    }

    @Test
    void broadcastAiResponseHandlesNetworkFailureGracefully() throws Exception {
        doThrow(new RuntimeException("network down"))
                .when(network)
                .broadcast(any(), eq(ModuleType.CHAT.ordinal()), eq(0));

        Method broadcaster =
                AiAnalyticsService.class.getDeclaredMethod("broadcastAiResponse", String.class);
        broadcaster.setAccessible(true);

        assertDoesNotThrow(() -> broadcaster.invoke(service, "Answer"));

        verify(network).broadcast(any(), eq(ModuleType.CHAT.ordinal()), eq(0));
        verify(rpc, never()).call(eq("chat:new-message"), any());

        List<ChatMessage> history = getHistory();
        assertFalse(history.isEmpty());
    }

    private List<ChatMessage> getHistory() throws Exception {
        @SuppressWarnings("unchecked")
        List<ChatMessage> history =
                (List<ChatMessage>) getFieldValue("fullMessageHistory");
        return history;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getSenderMap() throws Exception {
        return (Map<String, String>) getFieldValue("messageIdToSender");
    }

    private Object getFieldValue(String fieldName) throws Exception {
        Field field = AiAnalyticsService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(service);
    }

    private void invokeProcessIncrementalSummary() throws Exception {
        Method method =
                AiAnalyticsService.class.getDeclaredMethod("processIncrementalSummary");
        method.setAccessible(true);
        method.invoke(service);
    }
}

