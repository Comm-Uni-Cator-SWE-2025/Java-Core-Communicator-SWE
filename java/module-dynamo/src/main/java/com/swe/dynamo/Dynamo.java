package com.swe.dynamo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

import com.swe.core.ClientNode;
import com.swe.core.RPCinterface.AbstractRPC;

public class Dynamo {
    // Singleton instance
    private static final Dynamo INSTANCE = new Dynamo();

    private Coil socketry;

    // Private constructor to prevent instantiation
    private Dynamo() {
        try {
            socketry = new Coil();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Public accessor for singleton instance
    public static Dynamo getInstance() {
        return INSTANCE;
    }

    public void addUser(ClientNode self, ClientNode mainServer) {
        if (self.equals(mainServer)) {
            // self is the main server
            // add self to the main server
            // add self to the dynamo
        } else {
            // self is a client
            // add self to the client
            // add self to the dynamo
        }
    }

    public void closeDynamo() {
        // close the dynamo
    }

    public void consumeRPC(AbstractRPC rpc) {
        // consume the rpc
    }

    public void sendData(byte[] data, ClientNode[] destIp, int module, int priority) {
        // send the data to the destination
    }

    public void broadcast(byte[] data, int module, int priority) {
        // broadcast the data to all clients
    }

    public void subscribe(int name, Function<byte[], Void> function) {
        // subscribe to the name
    }

    public void removeSubscription(int name) {
        // remove the subscription
    }


    private void startListening() throws IOException {
        while (true) {
            // since each are configured in non-blocking mode
            // they just returns back almost instantly
            ArrayList<Packet> unhandledPackets = socketry.listen();
            unhandledPackets.forEach(packet -> {
                handlePacket(packet, tunnel);
            });
        }
    }

}
