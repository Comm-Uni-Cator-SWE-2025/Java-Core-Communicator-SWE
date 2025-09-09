/**
 * Implement
 * > A simple transferPacket which receives packet from the
 * other cluster servers and send it to the respective cluster
 *
 */


public interface AbstractTopology {
    void sendPacket(byte[] data, String sourceIp, Integer sourcePort, String destIp, Integer destPort);
}
