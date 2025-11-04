package com.swe.chat;

import com.swe.chat.AbstractRPC; // You will need to import your RPC files
import com.swe.chat.DummyRPC;   // You will need to import your RPC files
import com.swe.networking.ClientNode;
import com.swe.networking.SimpleNetworking.SimpleNetworking;
import javax.swing.SwingUtilities;

/**
 * Main application launcher for testing the refactored RPC architecture.
 * This class now acts as the "Controller" to inject dependencies.
 */
public class App {

    public static void main(final String[] args) {

        // --- THIS IS THE NEW CONTROLLER LOGIC ---

        // 1. Create the single RPC instance
        // (Make it final so the lambda can use it)
        final AbstractRPC dummyRpc = new DummyRPC();

        // 2. Create and configure the "Backend" (Core)
        // TODO: Update these IPs/Ports for your test
        // (This is the old code from ChatViewModel's constructor)
        SimpleNetworking network = SimpleNetworking.getSimpleNetwork();
        ClientNode device = new ClientNode("127.0.0.1", 1234); // This instance's port
        ClientNode server = new ClientNode("127.0.0.1", 1234); // The server's port
        network.addUser(device, server);

        // 3. Create the ChatManager (Core) and subscribe it to the RPC
        // This is the step that was missing!
        ChatManager chatManager = new ChatManager(dummyRpc, network);

        // ---

        // 4. Create the "Frontend" (View/ViewModel)
        SwingUtilities.invokeLater(() -> {

            // 5. Create the ViewModel and subscribe it to the RPC
            ChatViewModel viewModel = new ChatViewModel(dummyRpc);

            // 6. Create the View and inject the ViewModel
            // (This requires you to update ChatView's constructor)
            ChatView chatView = new ChatView(viewModel);

            chatView.setVisible(true);
        });
    }
}