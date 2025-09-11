import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Implement the following.
 * > a send function from client and server perspective
 * > a receive function from the server
 * > The server send function can use the interface abstractTopology
 * to send it to the topology for sending to another cluster
 * */

public class ClientServer {
    /**
     * A variable for: Is this client a server?.
     */
    private boolean isServer;

    /**
     * Socket reserver for this client/server.
     */
    private DatagramSocket socket;

    // open a udp server socket for this client
    public  ClientServer() {
        this.isServer = false;
        try {
            this.socket = new DatagramSocket();
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // TODO: wait for routing packet from the server

    /**
     * Topology server will implement this func.
     * @param dest Destination IP.
     */
    boolean destInCluster(final String dest) {
        return true;
    }

    /**
     * Function to send data to dest:port.
     * @param data Data to be sent across
     * @param dest Destination IP.
     * @param port Destination PORT.
     */
    public void recieveFrom() {
        // testing pending
        try {
            // Buffer to store incoming packet data
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            this.socket.receive(packet);

            // Extracting data, destination, and port
            String data = new String(packet.getData(), 0, packet.getLength());
            String dest = packet.getAddress().getHostAddress();
            int port = packet.getPort();

            if (server) {
                if (destInCluster(dest)) {
                    DatagramPacket response = new DatagramPacket(
                            data.getBytes(),
                            data.getBytes().length,
                            packet.getAddress(),
                            port
                    );
                    this.socket.send(response);
                } else {
                    throw new RuntimeException("Destination not in cluster: " + dest);
                }
            } else {
                // if it is client instead
                // TODO: callMessageListener()
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTo(final byte[] data, final String dest, final Integer port) {
        if (this.isServer) {
            if (destInCluster(dest)) {
                try {
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(dest), port);
                    try {
                        this.socket.send(sendPacket);
                    }  catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }  catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                try {
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, getServer(dest), port); // TODO: getServer to be implemented by Topology.
                    try {
                        this.socket.send(sendPacket);
                    }  catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }  catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            if (destInCluster(dest)) {
                try {
                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(dest), port);
                    try {
                        this.socket.send(sendPacket);
                    }  catch (IOException e) {
                        System.err.println("Client error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }  catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // TODO: get server ip from routing packet.
                // send(data, clusterServerIP, port);
            }
        }
    }
}
