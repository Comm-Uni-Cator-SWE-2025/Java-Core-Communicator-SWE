package com.swe.ScreenNVideo.PatchGenerator;

import java.nio.ByteBuffer;

public class CompressedPatch {
    private int x;
    private int y;
    private int width;
    private int height;
    private byte[] data; // compressed tile as a string

    public CompressedPatch(int x, int y, int width, int height, byte[] data) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    /** Serializes this packet to be sent via network.
     * @return byte[] to be sent
     */
    public byte[] serializeCPacket() {
        final int lenRequired = data.length + 16; // 4 * 4 variables + data
        final ByteBuffer buffer = ByteBuffer.allocate(lenRequired + 4);
        buffer.putInt(lenRequired);
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(width);
        buffer.putInt(height);

        buffer.put(data);

        return buffer.array();
    }

    /**
     * Creates a Compresses patch from incoming packet from networking module.
     * using byte[]
     * @param packet incoming data
     * @return CompressedPatch using the data
     */
    public static CompressedPatch deserializeCPacket(final byte[] packet) {
        final ByteBuffer buffer = ByteBuffer.wrap(packet);
        return deserializeCPacket(buffer, packet.length);
    }

    /**
     * Creates a Compresses patch from incoming packet from networking module.
     * using ByteBuffer.
     * Side Effects: Advances the buffer internal currentPos pointer.
     *
     * @param packetBuffer incoming data
     * @param packetLength length of data that corresponds to this packet
     * @return CompressedPatch using the data
     */
    public static CompressedPatch deserializeCPacket(final ByteBuffer packetBuffer, int packetLength) {
        final int x = packetBuffer.getInt();
        final int y = packetBuffer.getInt();
        final int width = packetBuffer.getInt();
        final int height = packetBuffer.getInt();
        final int variableSpaces = 16;
        final int dataLength = packetLength - variableSpaces;
        final byte[] data = new byte[dataLength];
        packetBuffer.get(data, variableSpaces, dataLength);
        return new CompressedPatch(
            x, y, width, height, data
        );
    }

    // getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getData() {
        return data;
    }
}
