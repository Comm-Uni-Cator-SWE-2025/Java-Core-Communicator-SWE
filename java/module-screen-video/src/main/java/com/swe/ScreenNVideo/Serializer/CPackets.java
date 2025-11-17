package com.swe.ScreenNVideo.Serializer;

import com.swe.ScreenNVideo.PatchGenerator.CompressedPatch;
import com.swe.ScreenNVideo.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Patches to be transferred over network.
 *
 * @param packetNumber Packet Number.
 * @param packets Packets transferred from different user.
 * @param ip      IP of the User who transferred these packets.
 * @param isFullImage whether this is a full image
 * @param height height of the image
 * @param width width of the image
 */
public record CPackets(int packetNumber, String ip, boolean isFullImage, int height, int width,
                       List<CompressedPatch> packets) {

    /**
     * Serializes List of CompressedPackets for networking layer.
     *
     * @return serialized byte array
     * @throws IOException Can have exception while writing to the outputStream while creating the buffer data
     */
    public byte[] serializeCPackets() throws IOException {

        final int len = packets.size();
        final ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        // Write the packet Type
        bufferOut.write((byte) NetworkPacketType.LIST_CPACKETS.ordinal());
        // Write the feed Number
        Utils.writeInt(bufferOut, packetNumber);
        // write if full image
        final byte fullImageFlag = isFullImage ? (byte) 1 : (byte) 0;
        bufferOut.write(fullImageFlag);
        // Write the height
        Utils.writeInt(bufferOut, height);
        // Write the width
        Utils.writeInt(bufferOut, width);
        // Write the IP of the user
        final byte[] ipBytes = ip.getBytes();
        Utils.writeInt(bufferOut, ipBytes.length);
        bufferOut.write(ipBytes);
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
    public static CPackets deserialize(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        // get packet type
        final byte packetType = buffer.get();

        if (packetType != NetworkPacketType.LIST_CPACKETS.ordinal()) {
            throw new InvalidParameterException(
                "Invalid Data type: Expected " + NetworkPacketType.LIST_CPACKETS.ordinal() + " got : " + packetType);
        }
        // get feed number
        final int packetNumber = buffer.getInt();
        // get if full image
        final boolean isFullImage = buffer.get() == 1;
        // get height
        final int height = buffer.getInt();
        // get width
        final int width = buffer.getInt();
        // get string len
        final int strLen = buffer.getInt();
        final byte[] ipByte = new byte[strLen];
        final int bufferstart = 0;
        buffer.get(ipByte, bufferstart, strLen);
        final String ip = new String(ipByte);

        // get patches length
        final int len = buffer.getInt();

        final List<CompressedPatch> patches = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            final int patchLength = buffer.getInt();
            // this will create the patch and
            // advances the buffer internal currentPos pointer
            final CompressedPatch patch = CompressedPatch.deserializeCPacket(buffer, patchLength);
            if (patch != null) {
                patches.add(patch);
            }
        }
        return new CPackets(packetNumber, ip, isFullImage, height, width, patches);
    }
}
