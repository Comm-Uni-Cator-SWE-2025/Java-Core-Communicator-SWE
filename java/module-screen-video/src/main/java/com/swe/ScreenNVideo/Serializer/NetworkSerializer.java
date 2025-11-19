/**
 * Contributed by @alonot
 */

package com.swe.ScreenNVideo.Serializer;


import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

/**
 * Serializer class for communicating with networking layer.
 */
public class NetworkSerializer {

    /**
     * Serializes the string for networking layer.
     * @param data the string to be sent
     * @param type the type of the network packet
     * @return serialized byte array
     */
    public static byte[] serializeIP(final NetworkPacketType type, final  String data) {
        final int len = data.length();
        final ByteBuffer buffer = ByteBuffer.allocate(len + 1);
        buffer.put((byte) (type.ordinal()));
        buffer.put(data.getBytes());
        return buffer.array();
    }

    /**
     * Deserializes the string from the networking layer.
     * @param data the byte array to be deserialized
     * @return the string
     */
    public static String deserializeIP(final byte[] data) {
        return new String(data, 1, data.length - 1);
    }

}
