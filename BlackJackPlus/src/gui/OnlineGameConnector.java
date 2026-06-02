package gui;

import javax.swing.*;
import backend.BlackjackClient;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;

public class OnlineGameConnector {
    private JFrame frame;
    private BlackjackClient client;

    private JLabel statusLabel;
    private JPanel playerCardPanel;
    private JPanel dealerCardPanel;
    private JButton hitButton;
    private JButton standButton;

    public OnlineGameConnector(BlackjackClient client) {
        this.client = client;
        initializeGUI();
        startServerListener();
    }

    private void initializeGUI() {
        frame = new JFrame("Blackjack+ Arena");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(20, 50, 30));
        mainPanel.setLayout(new BorderLayout());

        statusLabel = new JLabel("Waiting for game to start...", JLabel.CENTER);
        statusLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);
        mainPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new GridLayout(2, 1));
        tablePanel.setOpaque(false);

        dealerCardPanel = new JPanel();
        dealerCardPanel.setBorder(BorderFactory.createTitledBorder("Dealer's Hand"));

        playerCardPanel = new JPanel();
        playerCardPanel.setBorder(BorderFactory.createTitledBorder("Your Hand"));

        tablePanel.add(dealerCardPanel);
        tablePanel.add(playerCardPanel);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);

        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");

        hitButton.setEnabled(false);
        standButton.setEnabled(false);

        hitButton.addActionListener(e -> client.sendMove("hit"));
        standButton.addActionListener(e -> client.sendMove("stand"));

        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void startServerListener() {
        new Thread(() -> {
            try {
                BufferedReader in = client.getInputStream();
                String serverMessage;

                while ((serverMessage = in.readLine()) != null) {
                    final String msg = serverMessage;
                    SwingUtilities.invokeLater(() -> processServerMessage(msg));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Connection to server lost.");
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                });
            }
        }).start();
    }

    private void processServerMessage(String message) {
        System.out.println("[Server Data]: " + message);

        if (message.contains("GAME_STARTED")) {
            statusLabel.setText("Game Started! Good Luck!");
            hitButton.setEnabled(true);
            standButton.setEnabled(true);
            playerCardPanel.removeAll();
            dealerCardPanel.removeAll();
        }
        else if (message.contains("Initial Cards")
                || message.contains("Your current hand")
                || message.contains("hit:")) {
            JLabel cardLabel = new JLabel(message.replace("[Arena]: ", ""));
            cardLabel.setForeground(Color.YELLOW);
            playerCardPanel.add(cardLabel);
            playerCardPanel.revalidate();
            playerCardPanel.repaint();
        }
        else if (message.contains("Your Turn")) {
            statusLabel.setText("It's your turn!");
            hitButton.setEnabled(true);
            standButton.setEnabled(true);
        }
        else if (message.contains("Waiting")) {
            statusLabel.setText("Waiting for other players...");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }
        else if (message.contains("Bust")
                || message.contains("wins")
                || message.contains("Congratulations")) {
            statusLabel.setText(message);
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }
    }
}