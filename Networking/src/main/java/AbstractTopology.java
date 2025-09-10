/**
 * Implement
 * > A simple transferPacket which receives packet from the
 * other cluster servers and send it to the respective cluster
 *
 */


public interface AbstractTopology {
    ClientNode GetServer(String dest);
}
