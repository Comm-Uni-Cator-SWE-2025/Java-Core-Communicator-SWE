package com.swe.dynamo;

public class Node {
    private int IP;
    private short port;

    public Node(int IP, short port) {
        this.IP = IP;
        this.port = port;
    }

    public int getIP() {
        return IP;
    }
    
    public short getPort() {
        return port;
    }

    public byte[] serialize() {
        byte[] result = new byte[4 + 2];
        result[0] = (byte) ((IP >> 24) & 0xFF);
        result[1] = (byte) ((IP >> 16) & 0xFF);
        result[2] = (byte) ((IP >> 8) & 0xFF);
        result[3] = (byte) (IP & 0xFF);
        result[4] = (byte) ((port >> 8) & 0xFF);
        result[5] = (byte) (port & 0xFF);
        return result;
    }

    public static Node deserialize(byte[] data) {
        int IP = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        short port = (short) (((data[4] & 0xFF) << 8) | (data[5] & 0xFF));
        return new Node(IP, port);
    }
}
