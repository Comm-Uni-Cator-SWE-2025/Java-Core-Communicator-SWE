package com.swe.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.swe.core.RPCinterface.AbstractRPC;
import com.swe.networking.ModuleType;
import com.swe.networking.Networking;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatProcessorTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Mock
    private AbstractRPC rpc;

    @Mock
    private Networking network;

    @Mock
    private IChatFileHandler fileHandler;

    @Mock
    private IChatFileCache fileCache;

    @Mock
    private IAiAnalyticsService aiService;

    @Captor
    private ArgumentCaptor<byte[]> byteCaptor;

    private ChatProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ChatProcessor(rpc, network, fileHandler, fileCache, aiService);
        when(rpc.call(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(new byte[0]));
    }

    @Test
    void processFrontendTextMessageBroadcastsAndTracksHistory() {
        ChatMessage chatMessage = new ChatMessage("m1", "u1", "Alice", "Hi", null);
        byte[] serialized = ChatMessageSerializer.serialize(chatMessage);

        byte[] response = processor.processFrontendTextMessage(serialized);

        assertEquals(0, response.length);
        verify(aiService).addMessageToHistory(any(ChatMessage.class));
        verify(network).broadcast(byteCaptor.capture(), eq(ModuleType.CHAT.ordinal()), eq(0));
        assertEquals(ChatProtocol.FLAG_TEXT_MESSAGE, byteCaptor.getValue()[0]);
    }

    @Test
    void processFrontendTextMessageReturnsErrorWhenBroadcastFails() {
        ChatMessage chatMessage = new ChatMessage("m2", "u2", "Bob", "Yo", null);
        byte[] serialized = ChatMessageSerializer.serialize(chatMessage);
        doThrow(new RuntimeException("net-down"))
                .when(network).broadcast(any(), anyInt(), anyInt());

        byte[] response = processor.processFrontendTextMessage(serialized);

        assertTrue(new String(response, StandardCharsets.UTF_8).contains("net-down"));
    }

    @Test
    void processFrontendFileMessageCachesAndBroadcasts() throws Exception {
        FileMessage fileMessage =
                new FileMessage("file1", "user1", "Alice", "caption", "file.txt", "/tmp/a", "reply");
        byte[] serialized = FileMessageSerializer.serialize(fileMessage);
        byte[] compressed = new byte[] {1, 2, 3};
        when(fileHandler.processFileForSending("/tmp/a"))
                .thenReturn(new IChatFileHandler.FileResult(compressed, 100));

        byte[] response = processor.processFrontendFileMessage(serialized);

        assertEquals(0, response.length);
        verify(fileCache).put("file1", "file.txt", compressed);
        verify(rpc).call(eq("chat:file-metadata-received"), byteCaptor.capture());
        FileMessage metadata = FileMessageSerializer.deserialize(byteCaptor.getValue());
        assertEquals("file1", metadata.getMessageId());
        assertEquals("file.txt", metadata.getFileName());
        assertTrue(metadata.getFileContent() == null);

        verify(network).broadcast(byteCaptor.capture(), eq(ModuleType.CHAT.ordinal()), eq(0));
        assertEquals(ChatProtocol.FLAG_FILE_MESSAGE, byteCaptor.getValue()[0]);
    }

    @Test
    void processFrontendFileMessageReturnsErrorOnFailure() throws Exception {
        FileMessage fileMessage =
                new FileMessage("file2", "user2", "Cara", "caption", "f.bin", "/tmp/b", null);
        byte[] serialized = FileMessageSerializer.serialize(fileMessage);
        when(fileHandler.processFileForSending("/tmp/b"))
                .thenThrow(new Exception("compress fail"));

        byte[] response = processor.processFrontendFileMessage(serialized);

        assertTrue(new String(response, StandardCharsets.UTF_8).contains("compress fail"));
    }

    @Test
    void processFrontendSaveRequestWritesFile() throws Exception {
        byte[] cached = {9, 9, 9};
        when(fileCache.get("mid"))
                .thenReturn(Optional.of(new IChatFileCache.FileCacheEntry("doc.txt", cached)));

        byte[] response = processor.processFrontendSaveRequest("mid".getBytes(StandardCharsets.UTF_8));

        assertEquals("File saved successfully!",
                new String(response, StandardCharsets.UTF_8));
        verify(fileHandler).decompressAndSaveFile("mid", "doc.txt", cached);
        verify(rpc).call(eq("chat:file-saved-success"), any());
    }

    @Test
    void processFrontendSaveRequestReturnsErrorWhenMissing() {
        when(fileCache.get("missing")).thenReturn(Optional.empty());

        byte[] response =
                processor.processFrontendSaveRequest("missing".getBytes(StandardCharsets.UTF_8));

        assertTrue(new String(response, StandardCharsets.UTF_8).contains("File not found"));
        verify(rpc).call(eq("chat:file-saved-error"), any());
    }

    @Test
    void processFrontendDeleteRemovesAndBroadcasts() {
        byte[] response = processor.processFrontendDelete("del".getBytes(StandardCharsets.UTF_8));

        assertEquals(0, response.length);
        verify(fileCache).remove("del");
        verify(network).broadcast(any(), eq(ModuleType.CHAT.ordinal()), eq(0));
        verify(rpc).call(eq("chat:message-deleted"), any());
    }

    @Test
    void processNetworkMessageIgnoresNullOrEmpty() {
        processor.processNetworkMessage(null);
        processor.processNetworkMessage(new byte[0]);

        verifyNoInteractions(aiService, fileHandler, fileCache, rpc, network);
    }

    @Test
    void processNetworkMessageHandlesTextFlag() {
        ChatMessage chatMessage = new ChatMessage("n1", "user", "Dana", "Hi", null);
        byte[] payload = ChatMessageSerializer.serialize(chatMessage);
        byte[] packet = ChatProtocol.addProtocolFlag(payload, ChatProtocol.FLAG_TEXT_MESSAGE);

        processor.processNetworkMessage(packet);

        verify(aiService).addMessageToHistory(any(ChatMessage.class));
        verify(rpc).call(eq("chat:new-message"), eq(payload));
    }

    @Test
    void processNetworkMessageHandlesFileFlag() {
        byte[] fileContent = {7, 7};
        FileMessage contentMessage =
                new FileMessage("fid", "uid", "Liz", "cap", "f.dat", fileContent, 1L, "reply");
        byte[] payload = FileMessageSerializer.serialize(contentMessage);
        byte[] packet = ChatProtocol.addProtocolFlag(payload, ChatProtocol.FLAG_FILE_MESSAGE);

        processor.processNetworkMessage(packet);

        verify(fileCache).put("fid", "f.dat", fileContent);
        verify(rpc).call(eq("chat:file-metadata-received"), any());
    }

    @Test
    void processNetworkMessageHandlesDeleteFlag() {
        byte[] payload = "rid".getBytes(StandardCharsets.UTF_8);
        byte[] packet = ChatProtocol.addProtocolFlag(payload, ChatProtocol.FLAG_DELETE_MESSAGE);

        processor.processNetworkMessage(packet);

        verify(fileCache).remove("rid");
        verify(rpc).call(eq("chat:message-deleted"), any());
    }

    @Test
    void processNetworkMessageLogsUnknownFlag() {
        processor.processNetworkMessage(new byte[] {(byte) 0x7F, 10});

        verifyNoInteractions(aiService);
        verify(rpc, never()).call(anyString(), any());
    }

    @Test
    void processNetworkMessageSwallowsExceptions() {
        ChatMessage chatMessage = new ChatMessage("err", "user", "Name", "Hi", null);
        byte[] payload = ChatMessageSerializer.serialize(chatMessage);
        byte[] packet = ChatProtocol.addProtocolFlag(payload, ChatProtocol.FLAG_TEXT_MESSAGE);
        doThrow(new RuntimeException("bad")).when(aiService).addMessageToHistory(any());

        processor.processNetworkMessage(packet);

        // No exception thrown to caller.
        verify(aiService).addMessageToHistory(any());
    }
}

