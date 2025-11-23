package com.swe.dynamo.Parsers;

public class Chunk {
    private int messageID;
    private int chunkNumber;
    private byte[] payload;

    public Chunk(int messageID, int chunkNumber, byte[] payload) {
        this.messageID = messageID;
        this.chunkNumber = chunkNumber;
        this.payload = payload;
    }

    public int getMessageID() {
        return messageID;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public byte[] getPayload() {
        return payload;
    }

    /**
     * Serialises this Chunk to a byte array.
     * The first 4 bytes are messageID, next 4 are chunkNumber, rest is payload.
     */
    public byte[] serialize() {
        byte[] result = new byte[8 + (payload != null ? payload.length : 0)];

        // Write messageID (big endian)
        result[0] = (byte) ((messageID >> 24) & 0xFF);
        result[1] = (byte) ((messageID >> 16) & 0xFF);
        result[2] = (byte) ((messageID >> 8) & 0xFF);
        result[3] = (byte) (messageID & 0xFF);

        // Write chunkNumber (big endian)
        result[4] = (byte) ((chunkNumber >> 24) & 0xFF);
        result[5] = (byte) ((chunkNumber >> 16) & 0xFF);
        result[6] = (byte) ((chunkNumber >> 8) & 0xFF);
        result[7] = (byte) (chunkNumber & 0xFF);

        if (payload != null && payload.length > 0) {
            System.arraycopy(payload, 0, result, 8, payload.length);
        }

        return result;
    }

    /**
     * Deserializes a byte array into a Chunk object.
     * Expects first 4 bytes as messageID, next 4 as chunkNumber, rest as payload.
     */
    public static Chunk deserialize(byte[] data) {
        if (data == null || data.length < 8) {
            throw new IllegalArgumentException("Data too short to be a valid Chunk");
        }
        // Read messageID (big endian)
        int messageID = ((data[0] & 0xFF) << 24) |
                        ((data[1] & 0xFF) << 16) |
                        ((data[2] & 0xFF) << 8) |
                        (data[3] & 0xFF);
        // Read chunkNumber (big endian)
        int chunkNumber = ((data[4] & 0xFF) << 24) |
                          ((data[5] & 0xFF) << 16) |
                          ((data[6] & 0xFF) << 8) |
                          (data[7] & 0xFF);

        byte[] payload = null;
        if (data.length > 8) {
            payload = new byte[data.length - 8];
            System.arraycopy(data, 8, payload, 0, payload.length);
        }
        return new Chunk(messageID, chunkNumber, payload);
    }
}
