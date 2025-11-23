package com.swe.dynamo;


import com.swe.core.ClientNode;

public class SanityTest {

    public static void main(String[] args) throws Exception {
        Dynamo dynamo = Dynamo.getInstance();
        dynamo.registerDisconnectHandler(() -> {
            System.err.println("We seem disconnected from the network.");
        });
        dynamo.addUser(new ClientNode("10.128.5.156", 1212), new ClientNode("10.128.5.156", 1212));
        System.out.println("Connected to the network.");


        dynamo.subscribe(1, (data) -> {
            System.out.println("Received data: " + new String(data));
            return null;
        });

        dynamo.sendData("Hello, world!".getBytes(), new ClientNode[] { new ClientNode("10.128.6.193", 1212) }, 1, 0);
        System.out.println("Sent data to the network.");

        Thread.sleep(10000);

        dynamo.closeDynamo();
        System.out.println("Closed the network.");
    }
}
