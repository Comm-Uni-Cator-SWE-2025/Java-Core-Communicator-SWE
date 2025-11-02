package com.swe.chat;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// We no longer need MouseAdapter or MouseEvent
import java.util.ArrayList;
import java.util.List;

/**
 * The ONE View, built with Java Swing.
 * This class REPLACES ChatView.fxml and ChatController.java.
 * It creates and binds to the ChatViewModel.
 */
public class ChatView extends JFrame {

    // --- The ViewModel ---
    private final ChatViewModel viewModel;

    // --- UI Components ---
    private final JPanel messageContainer; // This is our VBox
    private final JScrollPane scrollPane;
    private final JTextField messageInputField;
    private final JButton sendButton;
    private final JButton testRecvButton; // From your FXML
    private final JPanel replyQuotePanel;
    private final JLabel replyQuoteLabel;

    // Store the message data for the "reply" feature
    private final List<ChatViewModel.MessageVM> messageVMs = new ArrayList<>();

    // We refer to the helper class as ChatViewModel.MessageVM

    public ChatView() {
        // 1. Create the ViewModel
        this.viewModel = new ChatViewModel();

        // 2. Set up the main window
        setTitle("CommUniCator Chat (Swing)");
        setSize(400, 600);
        setMinimumSize(new Dimension(350, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Optional: add an icon (place an "icon.png" in your resources folder)
        // setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());

        // Set a modern "Look and Feel" to be visually appealing
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Main Layout ---
        getContentPane().setLayout(new BorderLayout(0, 0));

        // 3. Create Components

        // --- Message List (Center) ---
        messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
        messageContainer.setBackground(Color.WHITE); // A clean, modern background

        JPanel messageContainerWrapper = new JPanel(new BorderLayout());
        messageContainerWrapper.setBackground(Color.WHITE);
        messageContainerWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        messageContainerWrapper.add(messageContainer, BorderLayout.NORTH);

        scrollPane = new JScrollPane(messageContainerWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xCCCCCC)));

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Panel ---
        JPanel bottomVBox = new JPanel();
        bottomVBox.setLayout(new BoxLayout(bottomVBox, BoxLayout.Y_AXIS));

        // --- Reply Quote Panel (from your FXML) ---
        replyQuotePanel = new JPanel(new BorderLayout(5, 5));
        replyQuotePanel.setBackground(new Color(0xEFEFEF));
        replyQuotePanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xCCCCCC)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        replyQuoteLabel = new JLabel("Replying to...");
        replyQuoteLabel.setForeground(new Color(0x555555));
        replyQuoteLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        JButton cancelReplyButton = new JButton("X");
        cancelReplyButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelReplyButton.setMargin(new Insets(2, 4, 2, 4));

        replyQuotePanel.add(replyQuoteLabel, BorderLayout.CENTER);
        replyQuotePanel.add(cancelReplyButton, BorderLayout.EAST);
        replyQuotePanel.setVisible(false); // Start hidden
        bottomVBox.add(replyQuotePanel);

        // --- Input HBox (from your FXML) ---
        JPanel inputHBox = new JPanel(new BorderLayout(10, 10));
        inputHBox.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding

        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageInputField.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0xAAAAAA)),
                new EmptyBorder(5, 5, 5, 5) // Inner padding
        ));

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setBackground(new Color(0x00, 0x7B, 0xFF));
        sendButton.setForeground(Color.WHITE);
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);

        testRecvButton = new JButton("Test Recv"); // Your test button
        testRecvButton.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0)); // Panel for buttons
        buttonPanel.add(sendButton);
        buttonPanel.add(testRecvButton);

        inputHBox.add(messageInputField, BorderLayout.CENTER);
        inputHBox.add(buttonPanel, BorderLayout.EAST);
        bottomVBox.add(inputHBox);

        getContentPane().add(bottomVBox, BorderLayout.SOUTH);

        // 4. Bind Actions from View to ViewModel

        sendButton.addActionListener(e -> viewModel.sendMessage(messageInputField.getText()));
        messageInputField.addActionListener(e -> viewModel.sendMessage(messageInputField.getText()));
//        testRecvButton.addActionListener(e -> viewModel.simulateIncomingMessage());
        cancelReplyButton.addActionListener(e -> viewModel.cancelReply());

        // 5. Bind Callbacks from ViewModel to View

        viewModel.setOnClearInput(() -> {
            messageInputField.setText("");
        });

        viewModel.setOnReplyStateChange(quoteText -> {
            if (quoteText != null) {
                replyQuoteLabel.setText(quoteText);
                replyQuotePanel.setVisible(true);
            } else {
                replyQuotePanel.setVisible(false);
            }
            replyQuotePanel.revalidate();
            replyQuotePanel.repaint();
        });

        viewModel.setOnMessageAdded(messageVM -> {
            messageVMs.add(messageVM); // Add to our list for the reply feature
            Component messageComponent = createMessageComponent(messageVM);
            messageContainer.add(messageComponent);

            messageContainer.revalidate();
            messageContainer.repaint();

            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMaximum());
            });
        });
    }

    /**
     * This private method builds a "chat bubble" component for a single message.
     */
    private Component createMessageComponent(ChatViewModel.MessageVM messageVM) {
        // --- Bubble Panel ---
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12)); // Padding
        bubble.setMaximumSize(new Dimension(300, 9999)); // Max width

        // --- Quote Panel (if it exists) ---
        if (messageVM.hasQuote()) {
            JPanel quotePanel = new JPanel(new BorderLayout());
            quotePanel.setBackground(new Color(0xDDDDDD));

            JLabel quoteLabel = new JLabel(messageVM.getQuotedContent());
            quoteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            quoteLabel.setForeground(new Color(0x333333));
            quotePanel.add(quoteLabel, BorderLayout.CENTER);

            quotePanel.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(0x007BFF)),
                    new EmptyBorder(3, 5, 3, 5)
            ));

            bubble.add(quotePanel);
            bubble.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        }

        // --- Username Label ---
        JLabel usernameLabel = new JLabel(messageVM.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));

        // --- Content Label ---
        JLabel contentLabel = new JLabel("<html><p style=\"width:220px;\">" + messageVM.getContent() + "</p></html>");
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // --- Timestamp Label ---
        JLabel timestampLabel = new JLabel(messageVM.getTimestamp());
        timestampLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timestampLabel.setForeground(Color.GRAY);
        timestampLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Reply Button ---
        JButton replyButton = new JButton("Reply");
        replyButton.setFont(new Font("Arial", Font.PLAIN, 10));
        replyButton.setMargin(new Insets(1, 5, 1, 5));
        replyButton.setOpaque(false); // Make it blend
        replyButton.setContentAreaFilled(false);
        replyButton.setFocusPainted(false); // Don't show focus ring

        // --- Bottom Row (Timestamp and Reply) ---
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false); // Make it transparent
        bottomRow.add(timestampLabel, BorderLayout.WEST);
        bottomRow.add(replyButton, BorderLayout.EAST);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add components to bubble
        bubble.add(usernameLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 2))); // Spacer
        bubble.add(contentLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        bubble.add(bottomRow); // Add the new bottom row

        // --- Wrapper Panel ---
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(3, 0, 3, 0));

        // Set colors and alignment
        if (messageVM.isSentByMe()) {
            bubble.setBackground(new Color(0xE1, 0xF5, 0xFE)); // Light blue for "sent"
            usernameLabel.setForeground(new Color(0x00, 0x5A, 0x9E)); // Darker blue
            wrapper.add(bubble, BorderLayout.EAST);
        } else {
            bubble.setBackground(new Color(0xF1, 0xF1, 0xF1)); // Lighter gray for "received"
            usernameLabel.setForeground(new Color(0x00, 0x7B, 0xFF)); // Standard blue
            wrapper.add(bubble, BorderLayout.WEST);
        }

        // --- ADD ACTION TO THE REPLY BUTTON ---
        // (Replaces the old MouseAdapter)
        replyButton.addActionListener(e -> {
            viewModel.startReply(messageVM);
            messageInputField.requestFocus();
        });

        return wrapper;
    }
}