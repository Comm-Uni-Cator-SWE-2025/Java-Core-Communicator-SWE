package com.swe.networking.SimpleNetworking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import com.swe.networking.ClientNode;
import com.swe.networking.ModuleType;

public class Client implements IUser {
    private String deviceIp;
    private int devicePort;
    private Socket sendSocket = new Socket();
    private ServerSocket receiveSocket;
    private PacketParser parser;
    private SimpleNetworking simpleNetworking;
    private ModuleType moduleType = ModuleType.NETWORKING;

    public Client(ClientNode deviceAddr) {
        deviceIp = deviceAddr.hostName();
        devicePort = deviceAddr.port();
        parser = PacketParser.getPacketParser();
        simpleNetworking = SimpleNetworking.getSimpleNetwork();
        try {
            receiveSocket = new ServerSocket(devicePort);
            receiveSocket.setSoTimeout(0);
        } catch (IOException e) {
            System.err.println("Client1 Error: " + e.getMessage());
        }
    }

    @Override
    public void send(byte[] data, ClientNode[] destIp, ClientNode serverIp) {
        for (ClientNode client : destIp) {
            String ip = client.hostName();
            int port = client.port();
            try {
                sendSocket = new Socket();
                sendSocket.connect(new InetSocketAddress(ip, port), 5000);
                DataOutputStream dataOut = new DataOutputStream(sendSocket.getOutputStream());
                InetAddress addr = InetAddress.getByName(ip);
                dataOut.write(parser.createPkt(0, 0, 7, 0, 0, addr, port, data));
                System.out.println("Sent data succesfully...");
                sendSocket.close();
            } catch (IOException e) {
                System.err.println("Client2 Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void receive() throws IOException {
        try {
            Socket socket = receiveSocket.accept();
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            byte[] packet = dataIn.readAllBytes();
            parsePacket(packet);
        } catch (SocketTimeoutException e) {
            System.err.println("Client3 Error: " + e.getMessage());
        }
    }

    public void parsePacket(byte[] packet) {
        int module = parser.getModule(packet);
        ModuleType type = moduleType.getType(module);
        String data = new String(parser.getPayload(packet), StandardCharsets.UTF_8);
        System.out.println("Data received : " + data);
        simpleNetworking.callSubscriber(packet, type);
    }
}
