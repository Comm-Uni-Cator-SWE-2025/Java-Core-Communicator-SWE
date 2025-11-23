// module-chat/src/main/java/com/swe/chat/IChatProcessor.java

package com.swe.chat;

/**
 * ABSTRACTION: Defines the contract for all core chat and message processing logic.
 * Isolates message execution logic from the ChatManager router.
 */
public interface IChatProcessor {

    byte[] processFrontendTextMessage(byte[] messageBytes) throws Exception;
    byte[] processFrontendFileMessage(byte[] messageBytes) throws Exception;
    byte[] processFrontendSaveRequest(byte[] messageIdBytes) throws Exception;
    byte[] processFrontendDelete(byte[] messageIdBytes) throws Exception;
    void processNetworkMessage(byte[] networkPacket);
}