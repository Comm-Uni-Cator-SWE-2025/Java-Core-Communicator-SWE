// This File is changed by Vishal Rahangdale.

import java.util.ArrayList;
import java.util.List;

/**
 * A simple data class to hold client information (hostname and port).
 * This is the Java equivalent of a tuple(string, port).
 * @param hostName The IP address.
 * @param port The PORT.
 */
record ClientNode(String hostName, int port) {}

/**
 * Manages a collection of clients, designating the first one added as the server.
 */
public class Cluster {
    /**
     * Server's IP Address.
     */
    private String serverHostName;

    /**
     * Server's PORT number.
     */
    private int serverPortNumber;

    /**
     * The list holds all clients in the cluster, including the one designated as the server.
     * It's initialized immediately to prevent NullPointerException.
     */
    private final List<ClientNode> clients = new ArrayList<>();

    /**
     * Adds a new client to the cluster.
     * If this is the first client, it is designated as the server.
     *
     * @param hostName The hostname or IP address of the client.
     * @param port The port number of the client.
     */
    public void addClient(final String hostName, final int port) {
        // Check if the list is empty to see if this is the first client.
        if (clients.isEmpty()) {
            this.serverHostName = hostName;
            this.serverPortNumber = port;
        }

        // Create a new ClientNode and add it to the list.
        clients.add(new ClientNode(hostName, port));

        // TODO: Implement logic to send routing packet to each client with the server details.
    }

    /**
     * Returns the client that was designated as the server (the first one added).
     *
     * @return A ClientNode object representing the server, or null if the cluster is empty.
     */
    public ClientNode getServerName() {
        if (clients.isEmpty()) {
            return null; // Return null if no clients have been added yet.
        }
        // The server is always the first element in the list.
        return clients.get(0);
    }
}
