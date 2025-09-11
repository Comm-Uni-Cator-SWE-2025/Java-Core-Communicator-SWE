package com.swe.networking;/* Parser for the packets.
The structure of the packet is given below 
- Type     : 2bits
- Priority : 3bits
- Module   : 4bits 
- empty    : 7bits ( for future use )

 0                             1
 0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6  7  8  9...
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--
|Type |Priority|   Module  |      empty         | Payload...
+----------+-----------+----------------+-------------------
2 bits  3 bits    4 bits          7 bits           variable
*/

public class PacketParser {

    public static int getType(byte[] pkt) {
        return (pkt[0] >> 6) & 0b11;
    }

    public static int getPriority(byte[] pkt) {
        return (pkt[0] >> 3) & 0b111;
    }

    public static int getModule(byte[] pkt) {
        return ((pkt[0] & 0b111) << 1) | ((pkt[1] >> 7) & 0b1);
    }

    public static byte[] getPayload(byte[] pkt) {
        byte[] payload = new byte[pkt.length - 2];
        System.arraycopy(pkt, 2, payload, 0, payload.length);
        return payload;
    }

    public static byte[] createPkt(int type, int priority, int module, byte[] data) {
        byte[] pkt = new byte[data.length + 2];
        // Byte 0 = Type (2b) + Priority (3b) + Module[3:1] (3b)
        pkt[0] = (byte) (((type & 0b11) << 6) |
                ((priority & 0b111) << 3) |
                ((module & 0b1110) >> 1));

        // Byte 1 = Module[0] (1b) + Empty (7b)
        pkt[1] = (byte) ((module & 0b1) << 7);
        System.arraycopy(data, 0, pkt, 2, data.length);
        return pkt;
    }
}
