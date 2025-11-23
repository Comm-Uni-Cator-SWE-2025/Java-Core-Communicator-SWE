package com.swe.dynamo;


import java.util.Scanner;

import com.swe.core.ClientNode;

public class SanityTest {

    public static void main(String[] args) throws Exception {
        Dynamo dynamo = Dynamo.getInstance();
        dynamo.registerDisconnectHandler(() -> {
            System.err.println("We seem disconnected from the network.");
        });
        dynamo.addUser(new ClientNode("10.128.5.145", 1212), new ClientNode("10.128.5.145", 1212));
        System.out.println("Connected to the network.");


        dynamo.subscribe(1, (data) -> {
            System.out.println("Received data: " + new String(data));
            return null;
        });

        Scanner sc = new Scanner(System.in);
        
        sc.nextLine();       

        // dynamo.sendData("Hello, world!".getBytes(), new ClientNode[] { new ClientNode("10.128.6.193", 1212) }, 1, 0);
        dynamo.broadcast("Hello, world!".getBytes(), 1, 0);
        System.out.println("Sent data to the network.");

        sc.nextLine();

        // a string with Hello World 100 times
        String helloWorld = "Hello, world!".repeat(1000000);
        dynamo.broadcast(helloWorld.getBytes(), 1, 0);
        System.out.println("Sent random data to the network.");

        sc.nextLine();

        dynamo.closeDynamo();
        System.out.println("Closed the network.");
    }
}
