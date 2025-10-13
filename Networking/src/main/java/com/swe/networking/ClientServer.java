package com.swe.networking;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static com.swe.networking.Topology.topology;

/**
 * Implement the following.
 * > a send function from client and server perspective
 * > a receive function from the server
 * > The server send function can use the interface abstractTopology
 * to send it to the topology for sending to another cluster
 *
 */

public class ClientServer {

    // record to hold client server information
// own ip address
// own port

// manually done by now later done by controller
// server ip address
// server port


    private InetAddress hostName;
    private int port;

    private InetAddress serverHostname;
    private int serverPort;

    record ClientNode(InetAddress hostName, int port) {};
    private final List<ClientNode> clients;

    private ServerSocket receiveSocket;
    private Socket sendSocket = new Socket();
    private BufferedReader readData;
    private PrintWriter writeData;


    public ClientServer(InetAddress _serverHostname, int _serverPort) {

        try {
            hostName=InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("Could not get IP address: " + e.getMessage());
            e.printStackTrace();
        }
        port=12000;

        serverHostname = _serverHostname;
        serverPort = _serverPort;

        clients = new List<ClientNode>();
;
        try {
            this.receiveSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("TCP server error: " + e.getMessage());
            e.printStackTrace();
        }


        if (serverHostname.equals(hostname)) {
            clients.add(new ClientNode(hostname, port));
        } else {
            sendHello(serverHostname, serverPort);
        }
    }




    private boolean destInCluster(InetAddress ip, int port) {
        return clients.contains(new ClientNode(ip, port));
    }




    public void receiveFrom() {
        // testing pending
        try {
            // Buffer to store incoming packet data
            byte[] buffer = new byte[1024];

            commSocket = this.receiveSocket.accept();
            DataInputStream dataIn = new DataInputStream(commSocket.getInputStream());
            byte[] packet = dataIn.readAllBytes();
            // Extracting data, destination, and port
            InetAddress dest = commSocket.getInetAddress();
            int port = commSocket.getPort();
            PacketParser parser = PacketParser.getPacketParser();
            System.out.println(dest);
            System.out.println(port);

//            if (this.MainServer) {
//                int type = parser.getType(packet);
//                int connectionType = parser.getConnectionType(packet);
//                if (type == 3 && connectionType == 0) {
//                    byte[] networkData = topology.getNetwork().toString().getBytes();
//                    sendTo(networkData, dest.toString(), port);
//                }
//            }
            if (serverHostname.equals(hostname)) {
                int type= parser.getType(packet);
                int connectionType = parser.getConnectionType(packet);

                if (type==3 &&  connectionType==0) {
                    receiveHello(parser.getPayload(packet));
                }

            } else {

                int type= parser.getType(packet);
                int connectionType = parser.getConnectionType(packet);

                if (type==3 &&  connectionType==0) {
                    receiveHello(parser.getPayload(packet));
                }

                // if it is client instead
                // TODO: callMessageListener()
            }
            System.out.println("Well it worked but dont know how....");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int sendTo(final byte[] data, final InetAddress dest, final int port) throws IOException {
        if (serverHostname.equals(hostname)) {
            if (destInCluster(dest,port)) {
                try {
                    sendSocket.connect(new InetSocketAddress(dest, port));
                    DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
//                try {
//                    final String hostAddress = topology.getServer(dest).hostName();
//                    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(hostAddress), port);
//                    // TODO: getServer to be implemented by Topology.
//                    sendSocket.connect(new InetSocketAddress(hostAddress, port));
//                    DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
//                    dataOut.write(data);
//                } catch (IOException e) {
//                    System.err.println("Client error: " + e.getMessage());
//                    e.printStackTrace();
//                }
            }
        } else {
            if (destInCluster(dest,port)) {
                try {
                    sendSocket.connect(new InetSocketAddress(dest, port));
                    DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                try{
                    sendSocket.connect(new InetSocketAddress(serverHostname, serverPort));
                    DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
                    dataOut.write(data);
                } catch (IOException e) {
                    System.err.println("Client error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private long createPacketHeader(int type, int priority, int module, int connectionType, int broadcast, int empty, int ip, int port) {
        long packetHeader = 0;
        packetHeader |= ((long) type << 62);
        packetHeader |= ((long) priority << 59);
        packetHeader |= ((long) module << 55);
        packetHeader |= ((long) connectionType << 52);
        packetHeader |= ((long) broadcast << 51);
        packetHeader |= ((long) empty << 48);
        packetHeader |= ((long) ip << 16);
        packetHeader |= port;

        return packetHeader;
    }

    private void sendHello(InetAddress receiverIp, int receiverPort){
        long packetHeader = createPacketHeader(3, 0, 0, 0, 0, 0, 0, 0);
        byte[] addressBytes = receiverIp.getAddress();
        int size = addressBytes.length;

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putLong(packetHeader);
        buffer.putInt(size);
        buffer.put(addressBytes);
        buffer.putInt(receiverPort);

        this.sendTo(buffer.array(), serverHostname, serverPort);
    }

    private void receiveHello(byte[] data){
        if(serverHostname.equals(hostname)){
            long packetHeader = createPacketHeader(3, 0, 0, 0, 0, 0, 0, 0);

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.putLong(packetHeader);
            buffer.put(data);

            for(client in clients){
                if (!client.hostName.equals(hostName)){
                    this.sendTo(buffer.array(), client.hostName, client.port);
                }
            }
        }

        // Get Size of IP
        byte[] sizeBytes = new byte[4];
        System.arraycopy(data, 0, sizeBytes, 0, 4);
        int size = Integer.parseInt(new String(sizeBytes, StandardCharsets.US_ASCII));

        // Get IP Address
        byte[] addressBytes = new byte[size];
        System.arraycopy(data, 5, addressBytes, 0, size);
        InetAddress ip = InetAddress.getByAddress(addressBytes);

        // Get Port
        byte[] portBytes = new byte[4];
        System.arraycopy(data, 5 + size, portBytes, 0, 4);
        int port = Integer.parseInt(new String(portBytes, StandardCharsets.US_ASCII));

        clients.add(new ClientNode(ip, port));

    }
}
