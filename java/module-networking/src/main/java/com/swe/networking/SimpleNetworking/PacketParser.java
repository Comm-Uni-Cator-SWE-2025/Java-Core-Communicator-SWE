package com.swe.networking.SimpleNetworking;

import java.net.InetAddress;
import java.net.UnknownHostException;

/* Parser for the packets.
The structure of the packet is given below
- Type              : 2bits
- Priority          : 3bits
- Module            : 4bits
- Connection Type   : 3bits
- Broadcast         : 1bit
- empty             : 3bits ( for future use )
- IPv4 addr         : 32bits
- port num          : 16bits


0                             1
0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|Type |Priority|   Module  |Con Type|BC|  empty |
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|                  IPv4 Address                 |
|                                               |
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|                 port number                   |
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
| Payload....
+--+--+--+--+-
*/

/**
 * Class for packet parser.
 */
public class PacketParser {
    /** Static class object for packet parser. */
    private static PacketParser parser = null;

    /** Private constructor class. */
    private PacketParser() {

    }

    /**
     * Function to get the packetparser.
     *
     * @return the singleton object.
     */
    public static PacketParser getPacketParser() {
        if (parser == null) {
            parser = new PacketParser();
        }
        return parser;
    }

    /**
     * Function to get the type of packet.
     *
     * @param pkt the packet
     * @return the type
     */
    public int getType(final byte[] pkt) {
        return (pkt[0] >> 6) & 0b11;
    }

    /**
     * Function to get the priority of packet.
     *
     * @param pkt the packet
     * @return the priority
     */
    public int getPriority(final byte[] pkt) {
        return (pkt[0] >> 3) & 0b111;
    }

    /**
     * Function to get the module of packet.
     *
     * @param pkt the packet
     * @return the module
     */
    public int getModule(final byte[] pkt) {
        return ((pkt[0] & 0b111) << 1) | ((pkt[1] >> 7) & 0b1);
    }

    /**
     * Function to get the connection type of packet.
     *
     * @param pkt the packet
     * @return the connection type
     */
    public int getConnectionType(final byte[] pkt) {
        return (pkt[1] >> 4) & 0b111;
    }

    /**
     * Function to get the broadcast of packet.
     *
     * @param pkt the packet
     * @return the broadcast
     */
    public int getBroadcast(final byte[] pkt) {
        return (pkt[1] >> 3) & 0b1;
    }

    /**
     * Function to get the ip address of packet.
     *
     * @param pkt the packet
     * @return the ip address
     */
    public InetAddress getIpAddress(final byte[] pkt) throws UnknownHostException {
        final byte[] ipBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) (pkt[2 + i] & 0xFF);
        }
        return InetAddress.getByAddress(ipBytes);
    }

    /**
     * Function to get the port of packet.
     *
     * @param pkt the packet
     * @return the port
     */
    public int getPortNum(final byte[] pkt) {
        return ((pkt[6] & 0xFF) << 8) | (pkt[7] & 0xFF);
    }

    /**
     * Function to get the payload of packet.
     *
     * @param pkt the packet
     * @return the payload
     */
    public byte[] getPayload(final byte[] pkt) {
        final byte[] payload = new byte[pkt.length - 8];
        System.arraycopy(pkt, 8, payload, 0, payload.length);
        return payload;
    }

    /**
     * Function to create the packet.
     *
     * @param type           the type of packet
     * @param priority       the priority of packet
     * @param module         the module of packet
     * @param connectionType the connection type of packet
     * @param broadCast      the broadcast of packet
     * @param ipAddr         the ip address of packet
     * @param portNum        the port of packet
     * @param data           the payload of packet
     * @return the packet
     */
    public byte[] createPkt(final int type, final int priority, final int module,
            final int connectionType, final int broadCast, final InetAddress ipAddr,
            final int portNum, final byte[] data) {
        final byte[] pkt = new byte[data.length + 8];
        // Byte 0 = Type (2b) + Priority (3b) + Module[3:1] (3b)
        pkt[0] = (byte) (((type & 0b11) << 6) |
                ((priority & 0b111) << 3) |
                ((module & 0b1110) >> 1));

        // Byte 1 = Module[0] (1b) + connectionType (3b) + broadCast (1b) + Empty (3b)
        pkt[1] = (byte) (((module & 0b1) << 7) |
                ((connectionType & 0b111) << 4) |
                ((broadCast & 0b1) << 3));

        // Byte 2..5
        final byte[] ipBytes = ipAddr.getAddress();
        System.arraycopy(ipBytes, 0, pkt, 2, 4);

        // Byte 6,7
        pkt[6] = (byte) (portNum >> 8);
        pkt[7] = (byte) (portNum & 0xFF);

        System.arraycopy(data, 0, pkt, 8, data.length);
        return pkt;
    }
}