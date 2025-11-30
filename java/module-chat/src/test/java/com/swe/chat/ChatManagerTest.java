package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.core.Context;
import com.swe.core.RPC;
import com.swe.networking.MessageListener;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatManagerTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Mock
    private Networking network;

    @Mock
    private IChatProcessor processor;

    @Mock
    private AiClientService aiClientService;

    private ChatManager chatManager;
    private MessageListener networkListener;
    private TestRpc rpcDouble;

    @BeforeEach
    void setUp() throws Exception {
        rpcDouble = new TestRpc();
        Context.getInstance().rpc = rpcDouble;

        setAiInstance(aiClientService);
        when(aiClientService.summariseText(any())).thenReturn(CompletableFuture.completedFuture("ok"));
        when(aiClientService.answerQuestion(any())).thenReturn(CompletableFuture.completedFuture("ok"));

        chatManager = new ChatManager(network);
        setField(chatManager, "processor", processor);

        ArgumentCaptor<MessageListener> listenerCaptor = ArgumentCaptor.forClass(MessageListener.class);
        verify(network).subscribe(eq(ModuleType.CHAT.ordinal()), listenerCaptor.capture());
        networkListener = listenerCaptor.getValue();
    }

    @AfterEach
    void tearDown() throws Exception {
        Context.getInstance().rpc = null;
        setAiInstance(null);
    }

    @Test
    void constructorRegistersRpcHandlers() {
        assertNotNull(rpcDouble.getHandler("chat:send-text"));
        assertNotNull(rpcDouble.getHandler("chat:send-file"));
        assertNotNull(rpcDouble.getHandler("chat:delete-message"));
        assertNotNull(rpcDouble.getHandler("chat:save-file-to-disk"));
    }

    @Test
    void handleFrontendTextMessageDelegatesToProcessor() throws Exception {
        byte[] request = "payload".getBytes(StandardCharsets.UTF_8);
        byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
        when(processor.processFrontendTextMessage(request)).thenReturn(response);

        byte[] result = rpcDouble.invoke("chat:send-text", request);

        assertArrayEquals(response, result);
        verify(processor).processFrontendTextMessage(request);
    }

    @Test
    void handleFrontendTextMessageReturnsErrorOnFailure() throws Exception {
        byte[] request = "payload".getBytes(StandardCharsets.UTF_8);
        when(processor.processFrontendTextMessage(request)).thenThrow(new RuntimeException("broken"));

        byte[] result = rpcDouble.invoke("chat:send-text", request);

        assertEquals("ERROR: broken", new String(result, StandardCharsets.UTF_8));
    }

    @Test
    void handleFrontendFileMessageDelegatesToProcessor() throws Exception {
        byte[] request = "file".getBytes(StandardCharsets.UTF_8);
        byte[] response = "done".getBytes(StandardCharsets.UTF_8);
        when(processor.processFrontendFileMessage(request)).thenReturn(response);

        byte[] result = rpcDouble.invoke("chat:send-file", request);

        assertArrayEquals(response, result);
        verify(processor).processFrontendFileMessage(request);
    }

    @Test
    void handleSaveFileToDiskDelegatesAndReturnsErrorOnFailure() throws Exception {
        byte[] request = "save".getBytes(StandardCharsets.UTF_8);
        byte[] response = "stored".getBytes(StandardCharsets.UTF_8);
        when(processor.processFrontendSaveRequest(request)).thenReturn(response);

        byte[] success = rpcDouble.invoke("chat:save-file-to-disk", request);
        assertArrayEquals(response, success);

        when(processor.processFrontendSaveRequest(request)).thenThrow(new RuntimeException("fail"));
        byte[] failure = rpcDouble.invoke("chat:save-file-to-disk", request);
        assertEquals("ERROR: fail", new String(failure, StandardCharsets.UTF_8));
    }

    @Test
    void handleDeleteMessageDelegatesAndHandlesFailure() throws Exception {
        byte[] request = "delete".getBytes(StandardCharsets.UTF_8);
        byte[] response = "removed".getBytes(StandardCharsets.UTF_8);
        when(processor.processFrontendDelete(request)).thenReturn(response);

        byte[] success = rpcDouble.invoke("chat:delete-message", request);
        assertArrayEquals(response, success);

        when(processor.processFrontendDelete(request)).thenThrow(new RuntimeException("bad"));
        byte[] failure = rpcDouble.invoke("chat:delete-message", request);
        assertEquals("ERROR: bad", new String(failure, StandardCharsets.UTF_8));
    }

    @Test
    void handleNetworkMessageRoutesPacket() {
        byte[] packet = {1, 2, 3};

        networkListener.receiveData(packet);

        verify(processor).processNetworkMessage(packet);
    }

    @Test
    void sendMessageCallsRpcWithSerializedPayload() {
        ChatMessage message = new ChatMessage("id", "user", "Name", "Hello", null);

        chatManager.sendMessage(message);

        assertEquals("chat:send-text", rpcDouble.getLastCallName());
        assertArrayEquals(ChatMessageSerializer.serialize(message), rpcDouble.getLastCallPayload());
    }

    @Test
    void sendMessageHandlesRpcFailures() {
        ChatMessage message = new ChatMessage("id", "user", "Name", "Hello", null);
        rpcDouble.setCallException(new RuntimeException("boom"));

        assertDoesNotThrow(() -> chatManager.sendMessage(message));
    }

    @Test
    void receiveMessageRoutesJsonThroughRpc() {
        String json = "{\"msg\":\"hi\"}";

        chatManager.receiveMessage(json);

        assertEquals("chat:new-message", rpcDouble.getLastCallName());
        assertArrayEquals(json.getBytes(StandardCharsets.UTF_8), rpcDouble.getLastCallPayload());
    }

    @Test
    void receiveMessageHandlesRpcFailures() {
        rpcDouble.setCallException(new RuntimeException("boom"));

        assertDoesNotThrow(() -> chatManager.receiveMessage("{\"msg\":1}"));
    }

    @Test
    void deleteMessageRoutesRequestThroughRpc() {
        chatManager.deleteMessage("msg-1");

        assertEquals("chat:delete-message", rpcDouble.getLastCallName());
        assertArrayEquals("msg-1".getBytes(StandardCharsets.UTF_8), rpcDouble.getLastCallPayload());
    }

    @Test
    void deleteMessageHandlesRpcFailures() {
        rpcDouble.setCallException(new RuntimeException("boom"));

        assertDoesNotThrow(() -> chatManager.deleteMessage("msg-2"));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = ChatManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void setAiInstance(AiClientService service) throws Exception {
        Field field = AiInstance.class.getDeclaredField("aiClientService");
        field.setAccessible(true);
        field.set(null, service);
    }

    private static final class TestRpc extends RPC {
        private final Map<String, Function<byte[], byte[]>> handlers = new HashMap<>();
        private String lastCallName;
        private byte[] lastCallPayload;
        private RuntimeException callException;

        @Override
        public void subscribe(String methodName, Function<byte[], byte[]> method) {
            handlers.put(methodName, method);
        }

        @Override
        public CompletableFuture<byte[]> call(final String methodName, final byte[] data) {
            lastCallName = methodName;
            lastCallPayload = data;
            if (callException != null) {
                throw callException;
            }
            return CompletableFuture.completedFuture(data);
        }

        Function<byte[], byte[]> getHandler(String name) {
            return handlers.get(name);
        }

        byte[] invoke(String name, byte[] payload) throws Exception {
            Function<byte[], byte[]> handler = handlers.get(name);
            assertTrue(handler != null, "Handler for " + name + " missing");
            return handler.apply(payload);
        }

        String getLastCallName() {
            return lastCallName;
        }

        byte[] getLastCallPayload() {
            return lastCallPayload;
        }

        void setCallException(RuntimeException exception) {
            this.callException = exception;
        }
    }
}

