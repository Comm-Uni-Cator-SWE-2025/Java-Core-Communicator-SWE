/**
 *  Contributed by @BhupathiVarun.
 */

package com.swe.ScreenNVideo.Model;


/*import com.fasterxml.jackson.annotation.JsonProperty;*/

import java.nio.ByteBuffer;
/*import java.util.Arrays;*/

/**
 * Subscribe Packet.
 */
public record SubscriberPacket(String email, boolean reqCompression) {


    /**
     * Serializes the string for networking layer.
     * @return serialized byte array
     */
    public static SubscriberPacket deserialize(final byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);


        final boolean reqCompression = buffer.get() == 1;
        final byte[] emailBytes = new byte[buffer.remaining()];
        buffer.get(emailBytes);

        return new SubscriberPacket(new String(emailBytes), reqCompression);
    }

    public byte[] serialize() {
        final byte[] emailBytes = email.getBytes();
        final int len = emailBytes.length + 1;
        final ByteBuffer buffer = ByteBuffer.allocate(len);

        buffer.put((byte) (reqCompression ? 1 : 0));
        buffer.put(emailBytes);

        return buffer.array();
    }

}
