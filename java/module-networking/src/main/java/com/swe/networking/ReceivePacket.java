package com.swe.networking;

import com.swe.core.ClientNode;

public record ReceivePacket(ClientNode sender, byte[] data) {

}
