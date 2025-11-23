package com.swe.dynamo;

import java.util.Arrays;

public class Node {
    private int IP;
    private short port;

    public Node(int IP, short port) {
        this.IP = IP;
        this.port = port;
    }

    public Node(String IP, short port) {
        this.IP = ipToInt(IP);
        this.port = port;
    }

    public int getIP() {
        return IP;
    }
    
    public short getPort() {
        return port;
    }

    public int getPortInt() {
        return ((int) port) & 0xFFFF;
    }

    @Override
    public int hashCode() {
        return (IP + ":" + port).hashCode();
    }

    public String IPToString() {
        return ((IP >> 24) & 0xFF) + "." + ((IP >> 16) & 0xFF) + "." + ((IP >> 8) & 0xFF) + "." + (IP & 0xFF);
    }

    /**
     * Converts the given IP address to an integer.
     * Considers that int is 32 bits signed integer.
     * @param ip the IP address to convert
     * @return the integer representation of the IP address
     */
    public static int ipToInt(String ip) {
        int result = 0;
        final int[] ipInts = Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt).toArray();
        for (int i = 0; i < ipInts.length; i++) {
            result = result << 8 | ipInts[i] & 0xFF;
        }
        return result;
    }

    @Override
    public String toString() {
        return IPToString() + ":" + getPortInt();
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
