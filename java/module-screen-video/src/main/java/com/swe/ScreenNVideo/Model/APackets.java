/**
 * Contributed by @aman112201041
 */

package com.swe.ScreenNVideo.Model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Audio Packets to be sent over networking layer.
 * @param data data to be sent
 * @param packetNumber packet number of the feed
 */
public record APackets(int packetNumber, byte[] data, String ip, int predictedPCM, int indexPCM) {
    /**
     * Serializes AudioPacket for networking layer.
     *
     * @return serialized byte array
     * @throws IOException Can have exception while writing to the outputStream while creating the buffer data
     */
    public byte[] serializeAPackets() throws IOException {

        final ByteBuffer buffer = ByteBuffer.allocate(1 + data.length + 7 * Integer.BYTES);
        // Write the packet Type
        buffer.put((byte) NetworkPacketType.APACKETS.ordinal());

        final int[] ipInts = Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt).toArray();
        for (int i = 0; i < ipInts.length; i++) {
            buffer.putInt(ipInts[i]);
        }

        buffer.putInt(packetNumber);

        buffer.putInt(predictedPCM);
        buffer.putInt(indexPCM);

        buffer.put(data);
        final byte[] output = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(output);
        return output;
    }


    /**
     * get the Audio Packet.
     *
     * @param data incoming data
     * @return Audio Packet.
     */
    public static APackets deserialize(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        // get packet type
        final byte packetType = buffer.get();

        if (packetType != NetworkPacketType.APACKETS.ordinal()) {
            throw new InvalidParameterException(
                "Invalid Data type: Expected " + NetworkPacketType.APACKETS.ordinal() + " got : " + packetType);
        }

        final int[] ipInts = new int[4];
        for (int i = 0; i < 4; i++) {
            ipInts[i] = buffer.getInt();
        }
        String sender_ip = Arrays.stream(ipInts).mapToObj(String::valueOf).collect(Collectors.joining("."));

        // get feed number
        final int packetNumber = buffer.getInt();

        // get predictedPCM
        final int predictedPCM = buffer.getInt();

        // get indexPCM
        final int indexPCM = buffer.getInt();

        final byte[] audioData = new byte[buffer.remaining()];
        buffer.get(audioData);

        return new APackets(packetNumber, audioData, sender_ip, predictedPCM, indexPCM);
    }
}
