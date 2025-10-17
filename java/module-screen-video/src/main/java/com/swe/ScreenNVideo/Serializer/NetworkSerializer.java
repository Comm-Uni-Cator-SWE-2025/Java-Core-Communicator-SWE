package com.swe.ScreenNVideo.Serializer;


import com.swe.ScreenNVideo.Utils;
import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializer class for communicating with networking layer.
 */
public class NetworkSerializer {

    /**
     * Serializes List of CompressedPackets for networking layer.
     *
     * @param packets Packets to be sent
     * @return serialized byte array
     * @throws IOException Can have exception while writing to the outputStream while creating the buffer data
     */
    public static byte[] serializeCPackets(final List<CompressedPatch> packets) throws IOException {

        final int len = packets.size();
        final ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        // Write the packet Type
        bufferOut.write((byte) NetworkPacketType.LIST_CPACKETS.ordinal());
        // Write the patches length
        Utils.writeInt(bufferOut, len);
        // write each packet
        for (CompressedPatch packet : packets) {
            bufferOut.write(packet.serializeCPacket());
        }

        return bufferOut.toByteArray();
    }

    /**
     * get the Compressed packets.
     *
     * @param data incoming data
     * @return list of CompressedPatch.
     */
    public static List<CompressedPatch> deserializeCPackets(final byte[] data)
        throws IndexOutOfBoundsException, InvalidParameterException {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        // get packet type
        final byte packetType = buffer.get();

        if (packetType != NetworkPacketType.LIST_CPACKETS.ordinal()) {
            throw new InvalidParameterException(
                "Invalid Data type: Expected " + NetworkPacketType.LIST_CPACKETS.ordinal() + " got : " + packetType);
        }
        // get patches length
        final int len = buffer.getInt();

        final List<CompressedPatch> patches = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            final int patchLength = buffer.getInt();
            // this will create the patch and
            // advances the buffer internal currentPos pointer
            patches.add(CompressedPatch.deserializeCPacket(buffer, patchLength));
        }
        return patches;
    }

    /**
     * Serializes the string for networking layer.
     * @param data the string to be sent
     * @return serialized byte array
     */
    public static byte[] serializeString(final  String data) {
        final int len = data.length();
        final ByteBuffer buffer = ByteBuffer.allocate(len + 1);
        buffer.put((byte) (NetworkPacketType.SUBSCRIBE_AS_VIEWER.ordinal()));
        buffer.put(data.getBytes());
        return buffer.array();
    }

    /**
     * Deserializes the string from the networking layer.
     * @param data the byte array to be deserialized
     * @return the string
     */
    public static String deserializeString(final byte[] data) {
        final byte packetType = data[0];
        if (packetType != NetworkPacketType.SUBSCRIBE_AS_VIEWER.ordinal()) {
            throw new InvalidParameterException(
                "Invalid Data type: Expected "
                    + NetworkPacketType.SUBSCRIBE_AS_VIEWER.ordinal() + " got : " + packetType);
        }
        return new String(data, 1, data.length - 1);
    }

}
