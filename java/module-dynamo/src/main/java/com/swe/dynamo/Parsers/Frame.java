package com.swe.dynamo.Parsers;

import java.util.Arrays;

import com.swe.dynamo.Node;

public class Frame {
    private int length;
    private byte type;
    private byte priority;
    private byte forwardingLength;
    private Node[] forwardingNodes;
    private byte[] payload;

    public Frame(int length, byte type, byte priority, byte forwardingLength, Node[] forwardingNodes, byte[] payload) {
        this.length = length;
        this.type = type;
        this.priority = priority;
        this.forwardingLength = forwardingLength;
        this.forwardingNodes = forwardingNodes;
        this.payload = payload;
    }

    public byte[] serialize() {
        byte[] result = new byte[4 + 1 + 1 + 1 + forwardingLength * 6 + (payload != null ? payload.length : 0)];
        result[0] = (byte) ((length >> 24) & 0xFF);
        result[1] = (byte) ((length >> 16) & 0xFF);
        result[2] = (byte) ((length >> 8) & 0xFF);
        result[3] = (byte) (length & 0xFF);
        result[4] = type;
        result[5] = priority;
        result[6] = forwardingLength;
        for (int i = 0; i < forwardingLength; i++) {
            System.arraycopy(forwardingNodes[i].serialize(), 0, result, 7 + i * 6, 6);
        }
        if (payload != null && payload.length > 0) {
            System.arraycopy(payload, 0, result, 7 + forwardingLength * 6, payload.length);
        }
        return result;
    }

    public static Frame deserialize(byte[] data) {
        int length = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        byte type = data[4];
        byte priority = data[5];
        byte forwardingLength = data[6];
        Node[] forwardingNodes = new Node[forwardingLength];
        for (int i = 0; i < forwardingLength; i++) {
            forwardingNodes[i] = Node.deserialize(Arrays.copyOfRange(data, 7 + i * 6, 7 + i * 6 + 6));
        }
        byte[] payload = null;
        if (data.length > 7 + forwardingLength * 6) {
            payload = Arrays.copyOfRange(data, 7 + forwardingLength * 6, data.length);
        }
        return new Frame(length, type, priority, forwardingLength, forwardingNodes, payload);
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte getType() {
        return type;
    }

    public byte getPriority() {
        return priority;
    }

    public byte getForwardingLength() {
        return forwardingLength;
    }

    public Node[] getForwardingNodes() {
        return forwardingNodes;
    }

    public int getLength() {
        return length;
    }

    /**
     * Appends the given payload to the current payload.
     * @param payload the payload to append
     * @return true if the payload completes the frame, false otherwise
     */
    public boolean appendPayload(byte[] payload) {
        if (this.payload == null) {
            this.payload = payload;
        } else {
            this.payload = Arrays.copyOf(this.payload, this.payload.length + payload.length);
            System.arraycopy(payload, 0, this.payload, this.payload.length - payload.length, payload.length);
        }
        return this.payload.length == this.length;
    }
}
