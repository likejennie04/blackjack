package gui;

import javax.swing.*;
import backend.BlackjackClient;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;


public class OnlineGameConnector {
    private JFrame frame;
    private BlackjackClient client;

    // --- UI Components ---
    private JLabel statusLabel;
    private JPanel playerCardPanel;
    private JPanel dealerCardPanel;
    private JPanel rosterPanel; 
    private JButton hitButton;
    private JButton standButton;
    private JButton returnButton;

    // Track the player whose turn is currently active
    private String currentTurnPlayerName = "Waiting..."; 

    public OnlineGameConnector(BlackjackClient client) {
        this.client = client;
        initializeGUI();
        startServerListener();
        
        // Notify the server about the selected avatar ID immediately upon connection
        client.sendMove("AVATAR_UPDATE:" + client.getAvatarId());
    }

    
    private void initializeGUI() {
        frame = new JFrame("Blackjack+ Arena");
        frame.setSize(900, 700); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 50, 30)); // Classic casino green background

        // --- Top Area: Status Display and Player Roster ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        statusLabel = new JLabel("Waiting for game to start...", JLabel.CENTER);
        statusLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        rosterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        rosterPanel.setOpaque(false);
        rosterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 50)));

        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(rosterPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- Center Area: Card Game Table Splits (Dealer vs Player) ---
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

        // --- Bottom Area: Action Controls Buttons ---
        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);

        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        returnButton = new JButton("Return to Menu");

        // Disabled by default until it is explicitly the player's turn
        hitButton.setEnabled(false);
        standButton.setEnabled(false);

        hitButton.addActionListener(e -> {
            SoundManager.hitButton();
            client.sendMove("hit");
        });
        standButton.addActionListener(e -> {
            SoundManager.standButton();
            client.sendMove("stand");
        });
        
        returnButton.addActionListener(e -> {
            SoundManager.buttonOne();
            client.disconnect();
            frame.dispose();
            new BlackjackStartWindow(); // Return to setup main menu
        });

        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        controlPanel.add(returnButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        
        // Initial draw of the player's own block before other peers connect
        updateVisualRoster(client.getPlayerName() + "," + client.getAvatarId());
        
        frame.setVisible(true);
    }

    /**
     * Spawns a background thread to consistently read messages from the server socket.
     */
    private void startServerListener() {
        new Thread(() -> {
            try {
                BufferedReader in = client.getInputStream();
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    final String msg = serverMessage;
                    // Always safely mutate Swing components on the Event Dispatch Thread (EDT)
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

    /**
     * Parses the roster data string received from the server and renders avatars horizontally.
     * Expected string format: "Player1,avatarId;Player2,avatarId;..."
     */
    private void updateVisualRoster(String rawRosterData) {
        rosterPanel.removeAll();

        // 1. Render the Dealer's Profile Block
        JPanel dealerBlock = new JPanel(new BorderLayout());
        dealerBlock.setOpaque(false);
        JLabel dealerAvatar = new JLabel();
        if (!AvatarManager.getAllAvatars().isEmpty()) {
            dealerAvatar.setIcon(AvatarManager.getAllAvatars().get(0)); 
        }
        dealerAvatar.setHorizontalAlignment(JLabel.CENTER);
        JLabel dealerNameLabel = new JLabel("Dealer (House)", JLabel.CENTER);
        dealerNameLabel.setFont(new Font("Times New Roman", Font.BOLD, 12));
        
        dealerBlock.add(dealerAvatar, BorderLayout.CENTER);
        dealerBlock.add(dealerNameLabel, BorderLayout.SOUTH);

        // Highlight dealer block if it's the house's turn
        if ("Dealer (House)".equals(currentTurnPlayerName)) {
            dealerNameLabel.setForeground(Color.YELLOW);
            dealerNameLabel.setText("Dealer ★ Active");
            dealerBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3, true)); 
        } else {
            dealerNameLabel.setForeground(Color.RED);
            dealerBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 0, 0, 80), 1, true));
        }
        rosterPanel.add(dealerBlock);

        // 2. Parse and Render Connected Players Blocks
        if (rawRosterData != null && !rawRosterData.isEmpty()) {
            String[] participants = rawRosterData.split(";");
            for (String part : participants) {
                if (part.trim().isEmpty()) continue;
                String[] tokens = part.split(",");
                if (tokens.length < 2) continue;
                
                String name = tokens[0].trim();
                int avatarId = 0;
                try { avatarId = Integer.parseInt(tokens[1].trim()); } catch(NumberFormatException ex){}

                JPanel pBlock = new JPanel(new BorderLayout());
                pBlock.setOpaque(false);
                
                JLabel pAvatar = new JLabel();
                if (!AvatarManager.getAllAvatars().isEmpty() && avatarId >= 0 && avatarId < AvatarManager.getAllAvatars().size()) {
                    pAvatar.setIcon(AvatarManager.getAllAvatars().get(avatarId));
                }
                pAvatar.setHorizontalAlignment(JLabel.CENTER);
                
                JLabel pNameLabel = new JLabel(name, JLabel.CENTER);
                pNameLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
                
                // Colors self green to distinguish from other players
                if (name.equals(client.getPlayerName())) {
                    pNameLabel.setText(name + " (You)");
                    pNameLabel.setForeground(Color.GREEN);
                } else {
                    pNameLabel.setForeground(Color.WHITE);
                }

                // Visual highlight if this player is currently making a move
                if (name.equals(currentTurnPlayerName)) {
                    pNameLabel.setText(pNameLabel.getText() + " ➔ Turn");
                    pNameLabel.setFont(new Font("Times New Roman", Font.BOLD, 12));
                    pBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3, true)); // Gold active frame
                } else {
                    if (name.equals(client.getPlayerName())) {
                        pBlock.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 0, 80), 1, true));
                    } else {
                        pBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 1, true));
                    }
                }

                pBlock.add(pAvatar, BorderLayout.CENTER);
                pBlock.add(pNameLabel, BorderLayout.SOUTH);
                rosterPanel.add(pBlock);
            }
        }
        
        // Save current roster structure to cache for re-rendering on turn updates
        rosterPanel.putClientProperty("raw_cache", rawRosterData);
        rosterPanel.revalidate();
        rosterPanel.repaint();
    }

    /**
     * Converts raw card tags (e.g., "9d", "As") into system resource file URLs.
     */
    private URL getCardImageURL(String cardCode) {
        cardCode = cardCode.trim().toLowerCase().replace("[", "").replace("]", "");
        if (cardCode.contains("hidden") || cardCode.isEmpty()) {
            return getClass().getResource("/image/card_back.png");
        }

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
        return getClass().getResource("/image/" + valueName + "_of_" + suitName + ".png"); 
    }

    /**
     * Generates a structural card wrapper with solid white background and drops the scaled PNG inside.
     */
    private void displayCard(JPanel panel, String cardCode) {
        URL imgURL = getCardImageURL(cardCode);
        
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            
            // White card container "Shell" to overlay transparent PNGs perfectly
            JPanel cardShell = new JPanel(new BorderLayout());
            cardShell.setPreferredSize(new Dimension(75, 105));
            cardShell.setBackground(Color.WHITE);
            cardShell.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 1, true));
            
            Image scaled = icon.getImage().getScaledInstance(65, 95, Image.SCALE_SMOOTH);
            JLabel cardLabel = new JLabel(new ImageIcon(scaled));
            
            cardShell.add(cardLabel, BorderLayout.CENTER);
            panel.add(cardShell);
        } else {
            // Text backup if resources fail to load
            JLabel errorLabel = new JLabel("[" + cardCode + "]");
            errorLabel.setForeground(Color.YELLOW);
            panel.add(errorLabel);
        }
    }

    /**
     * Core Command Interpreter: Processes inputs received from backend network packet messages.
     */
    private void processServerMessage(String message) {
        System.out.println("[Server Data]: " + message);

        // --- Turn Manager Control Packet ---
        if (message.contains("CURRENT_TURN:")) {
            String activeUser = message.substring(message.indexOf("CURRENT_TURN:") + 13).trim();
            this.currentTurnPlayerName = activeUser;
            
            // Refresh top panel to shift active gold boundaries to current player
            String cachedData = (String) rosterPanel.getClientProperty("raw_cache");
            updateVisualRoster(cachedData);
            
            // Strict action controls: Only enable interaction buttons if it matches your own name
            if (activeUser.equals(client.getPlayerName())) {
                statusLabel.setText("★ Your Turn! Make your move. ★");
                hitButton.setEnabled(true);
                standButton.setEnabled(true);
            } else {
                statusLabel.setText("Waiting for " + activeUser + " to complete their turn...");
                hitButton.setEnabled(false);
                standButton.setEnabled(false);
            }
        }

        // --- Roster Metadata Refresh Packet ---
        if (message.contains("ROSTER_UPDATE:")) {
            String data = message.substring(message.indexOf("ROSTER_UPDATE:") + 14).trim();
            updateVisualRoster(data);
        }
        
        // --- Game Setup Clear Flags ---
        else if (message.contains("GAME_STARTED")) {
            statusLabel.setText("Game Started! Good Luck!");
            playerCardPanel.removeAll();
            dealerCardPanel.removeAll();
        } 
        
        // --- Dealer Card Visual Rendering Stream ---
        else if (message.contains("DEALER_INFO:")) {
            dealerCardPanel.removeAll();
            String content = message.replace("[Arena]:", "").replace("DEALER_INFO:", "").trim();
            String[] cards = content.split(",");
            for (String card : cards) {
                displayCard(dealerCardPanel, card.trim());
            }
            dealerCardPanel.revalidate();
            dealerCardPanel.repaint();
        }
        
        // --- Local Player Card Visual Rendering Stream ---
        else if (message.contains("PLAYER_CARDS:") || message.contains("hit:")) {
            playerCardPanel.removeAll(); 
            String content = message.replace("[Arena]:", "").replace("PLAYER_CARDS:", "").trim();
            String[] cards = content.split(",");
            for (String card : cards) {
                displayCard(playerCardPanel, card.trim());
            }
            playerCardPanel.revalidate();
            playerCardPanel.repaint();
        }
        
        // --- Match Terminal State Evaluations ---
        else if (message.contains("Bust") || message.contains("wins") || message.contains("Congratulations")) {
            statusLabel.setText("<html><font color='yellow'>" + message + "</font></html>");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }
    }
}