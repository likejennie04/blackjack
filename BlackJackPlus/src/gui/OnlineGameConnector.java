package gui;

import javax.swing.*;
import backend.BlackjackClient;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;

public class OnlineGameConnector {
    private JFrame frame;
    private BlackjackClient client;

    private JLabel statusLabel;
    private JPanel playerCardPanel;
    private JPanel dealerCardPanel;
    private JButton hitButton;
    private JButton standButton;
    private JButton returnButton;

    public OnlineGameConnector(BlackjackClient client) {
        this.client = client;
        initializeGUI();
        startServerListener();
    }

    private void initializeGUI() {
        frame = new JFrame("Blackjack+ Arena");
        frame.setSize(800, 650); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 50, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        statusLabel = new JLabel("Waiting for game to start...", JLabel.CENTER);
        statusLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);

        JLabel avatarDisplay = new JLabel();
        if (!AvatarManager.getAllAvatars().isEmpty()) {
            avatarDisplay.setIcon(AvatarManager.getAllAvatars().get(client.getAvatarId()));
        }
        avatarDisplay.setBorder(BorderFactory.createTitledBorder(null, "YOU", 0, 0, null, Color.WHITE));

        topPanel.add(statusLabel, BorderLayout.CENTER);
        topPanel.add(avatarDisplay, BorderLayout.WEST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new GridLayout(2, 1));
        tablePanel.setOpaque(false);

        dealerCardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        dealerCardPanel.setBorder(BorderFactory.createTitledBorder(null, "Dealer's Hand", 0, 0, null, Color.WHITE));
        dealerCardPanel.setOpaque(false);

        playerCardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        playerCardPanel.setBorder(BorderFactory.createTitledBorder(null, "Your Hand", 0, 0, null, Color.WHITE));
        playerCardPanel.setOpaque(false);

        tablePanel.add(dealerCardPanel);
        tablePanel.add(playerCardPanel);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);

        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        returnButton = new JButton("Return to Menu");

        hitButton.setEnabled(false);
        standButton.setEnabled(false);

        hitButton.addActionListener(e -> client.sendMove("hit"));
        standButton.addActionListener(e -> client.sendMove("stand"));
        
        returnButton.addActionListener(e -> {
            client.disconnect();
            frame.dispose();
            new BlackjackStartWindow();
        });

        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        controlPanel.add(returnButton);
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
                    statusLabel.setText("Connection lost.");
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                });
            }
        }).start();
    }

    private String getCardImagePath(String cardCode) {
        cardCode = cardCode.trim().toLowerCase().replace("[", "").replace("]", "");
        if (cardCode.contains("hidden")) return "image/cards/card_back.png";
        if (cardCode.isEmpty()) return "image/cards/card_back.png";

        String valuePart = cardCode.substring(0, cardCode.length() - 1);
        char suitPart = cardCode.charAt(cardCode.length() - 1);

        String valueName;
        switch (valuePart) {
            case "a":  valueName = "ace"; break;
            case "j":  valueName = "jack"; break;
            case "q":  valueName = "queen"; break;
            case "k":  valueName = "king"; break;
            default:   valueName = valuePart; break;
        }

        String suitName;
        switch (suitPart) {
            case 'd': suitName = "diamonds"; break;
            case 'h': suitName = "hearts"; break;
            case 'c': suitName = "clubs"; break;
            case 's': suitName = "spades"; break;
            default:  suitName = "unknown"; break;
        }

        String fullPath = "image/cards/" + valueName + "_of_" + suitName + ".png";
        return fullPath; 
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
        else if (message.contains("DEALER_INFO:")) {
            dealerCardPanel.removeAll();
            String content = message.replace("DEALER_INFO:", "").trim();
            String[] cards = content.split(",");
            for (String card : cards) {
                displayCard(dealerCardPanel, card.trim());
            }
            dealerCardPanel.revalidate();
            dealerCardPanel.repaint();
        }
        else if (message.contains("PLAYER_CARDS:") || message.contains("hit:")) {
            playerCardPanel.removeAll(); 
            String content = message.replace("PLAYER_CARDS:", "").replace("[Arena]: ", "").trim();
            String[] cards = content.split(",");
            for (String card : cards) {
                displayCard(playerCardPanel, card.trim());
            }
            playerCardPanel.revalidate();
            playerCardPanel.repaint();
        }
        else if (message.contains("Your Turn")) {
            statusLabel.setText("It's your turn!");
        }
        else if (message.contains("Bust") || message.contains("wins") || message.contains("Congratulations")) {
            statusLabel.setText("<html><font color='red'>" + message + "</font></html>");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }
    }

    // Helper to render card with white background shell
    private void displayCard(JPanel panel, String cardCode) {
        String path = getCardImagePath(cardCode);
        ImageIcon icon = new ImageIcon(path);
        
        if (icon.getIconWidth() > 0) {
            // Card container for white background
            JPanel cardShell = new JPanel(new BorderLayout());
            cardShell.setPreferredSize(new Dimension(75, 105));
            cardShell.setBackground(Color.WHITE);
            // Add a subtle border to make it pop
            cardShell.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 1, true));
            
            Image scaled = icon.getImage().getScaledInstance(65, 95, Image.SCALE_SMOOTH);
            JLabel cardLabel = new JLabel(new ImageIcon(scaled));
            
            cardShell.add(cardLabel, BorderLayout.CENTER);
            panel.add(cardShell);
        } else {
            panel.add(new JLabel("[" + cardCode + "]"));
        }
    }
}