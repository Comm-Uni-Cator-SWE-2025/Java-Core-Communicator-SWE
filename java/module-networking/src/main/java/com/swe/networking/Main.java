package com.swe.networking;

import java.util.Scanner;

import com.swe.networking.SimpleNetworking.MessageListener;
import com.swe.networking.SimpleNetworking.SimpleNetworking;

public class Main {
    public static void main(String[] args) {
        String deviceIp = "10.32.0.41";
        int devicePort = 1234;
        ClientNode device = new ClientNode(deviceIp, devicePort);
        String serverIp = "10.32.0.41";
        int serverPort = 1234;
        ClientNode server = new ClientNode(serverIp, serverPort);
        SimpleNetworking network = SimpleNetworking.getSimpleNetwork();
        MessageListener func = (byte[] data) -> {
            System.out.println("Received data" + data);
        };
        network.addUser(device, server);
        network.subscribe(ModuleType.CHAT, func);
        String receiveIp = "10.32.10.226";
        int receivePort = 1234;
        ClientNode receive = new ClientNode(receiveIp, receivePort);
        String data = "Welcome to the new world.....";
        ClientNode[] dests = { receive };
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("q"))
                break;
            System.out.println("Input : " + input);
            network.sendData(input.getBytes(), dests, ModuleType.CHAT, 0);
        }
    }
}
