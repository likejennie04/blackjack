package gui;

import javax.swing.*;
import backend.BlackjackClient;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;

/**
 * Fully stabilized OnlineGameConnector.
 * Controls paths based strictly on unified server sequence triggers.
 */
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

    private String currentTurnPlayerName = "Waiting..."; 

    public OnlineGameConnector(BlackjackClient client) {
        this.client = client;
        initializeGUI();
        startServerListener();
        
        client.sendMove("AVATAR_UPDATE:" + client.getAvatarId());
    }

    private void initializeGUI() {
        frame = new JFrame("Blackjack+ Arena");
        frame.setSize(900, 700); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 50, 30));

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

        // Guarded components - locked until server grants access
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
            new BlackjackStartWindow(); 
        });

        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        controlPanel.add(returnButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        updateVisualRoster(client.getPlayerName() + "," + client.getAvatarId());
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

    private void updateVisualRoster(String rawRosterData) {
        rosterPanel.removeAll();

        // 1. Dealer Profile
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

        if (currentTurnPlayerName.equalsIgnoreCase("Dealer")) {
            dealerNameLabel.setForeground(Color.YELLOW);
            dealerNameLabel.setText("Dealer ★ Active");
            dealerBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3, true)); 
        } else {
            dealerNameLabel.setForeground(Color.RED);
            dealerBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 0, 0, 80), 1, true));
        }
        rosterPanel.add(dealerBlock);

        // 2. Players
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
                
                if (name.equalsIgnoreCase(client.getPlayerName().trim())) {
                    pNameLabel.setText(name + " (You)");
                    pNameLabel.setForeground(Color.GREEN);
                } else {
                    pNameLabel.setForeground(Color.WHITE);
                }

                if (name.equalsIgnoreCase(currentTurnPlayerName)) {
                    pNameLabel.setText(pNameLabel.getText() + " ➔ Turn");
                    pNameLabel.setFont(new Font("Times New Roman", Font.BOLD, 12));
                    pBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3, true)); 
                } else {
                    if (name.equalsIgnoreCase(client.getPlayerName().trim())) {
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
        
        rosterPanel.putClientProperty("raw_cache", rawRosterData);
        rosterPanel.revalidate();
        rosterPanel.repaint();
    }

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

    private void displayCard(JPanel panel, String cardCode) {
        URL imgURL = getCardImageURL(cardCode);
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            JPanel cardShell = new JPanel(new BorderLayout());
            cardShell.setPreferredSize(new Dimension(75, 105));
            cardShell.setBackground(Color.WHITE);
            cardShell.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 1, true));
            
            Image scaled = icon.getImage().getScaledInstance(65, 95, Image.SCALE_SMOOTH);
            JLabel cardLabel = new JLabel(new ImageIcon(scaled));
            
            cardShell.add(cardLabel, BorderLayout.CENTER);
            panel.add(cardShell);
        } else {
            JLabel errorLabel = new JLabel("[" + cardCode + "]");
            errorLabel.setForeground(Color.YELLOW);
            panel.add(errorLabel);
        }
    }

    private void processServerMessage(String message) {
        System.out.println("[Server Data]: " + message);

        if (message.contains("ROSTER_UPDATE:")) {
            String data = message.substring(message.indexOf("ROSTER_UPDATE:") + 14).trim();
            updateVisualRoster(data);
        }
        
        else if (message.contains("GAME_STARTED")) {
            statusLabel.setText("Game Started! Good Luck!");
            playerCardPanel.removeAll();
            dealerCardPanel.removeAll();
            playerCardPanel.revalidate();
            dealerCardPanel.revalidate();
        } 
        
        else if (message.contains("DEALER_INFO:")) {
            dealerCardPanel.removeAll();
            String content = message.substring(message.indexOf("DEALER_INFO:") + 12).trim();
            String[] cards = content.split(",");
            for (String card : cards) {
                if (!card.trim().isEmpty()) displayCard(dealerCardPanel, card.trim());
            }
            dealerCardPanel.revalidate();
            dealerCardPanel.repaint();
        }
        
        else if (message.contains("PLAYER_CARDS:") || message.contains("hit:")) {
            playerCardPanel.removeAll(); 
            String targetKey = message.contains("PLAYER_CARDS:") ? "PLAYER_CARDS:" : "hit:";
            String content = message.substring(message.indexOf(targetKey) + targetKey.length()).trim();
            
            if (content.contains("(")) {
                content = content.substring(0, content.indexOf("(")).trim();
            }
            
            String[] cards = content.split(",");
            for (String card : cards) {
                if (!card.trim().isEmpty()) displayCard(playerCardPanel, card.trim());
            }
            playerCardPanel.revalidate();
            playerCardPanel.repaint();
        }

        // =============================================================
        // 🎯 FOOLPROOF UNIFIED ATOMIC ACTIONS
        // =============================================================
        // Direct Action Unlocking Signals - Zero String Splitting Overlap
        if (message.contains("UNLOCK_ACTIONS_FOR_CLIENT")) {
            statusLabel.setText("★ Your Turn! Make your move. ★");
            hitButton.setEnabled(true);
            standButton.setEnabled(true);
            System.out.println("✅ UI UNLOCKED VIA EXPLICIT PACKET TOKEN.");
        } 
        else if (message.contains("LOCK_ACTIONS_FOR_CLIENT")) {
            statusLabel.setText("Waiting for other players...");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            System.out.println("❌ UI LOCKED VIA EXPLICIT PACKET TOKEN.");
        }
        
        // Dynamic Synchronization of Current Turn Names (For Avatar Highlighting only)
        if (message.contains("CURRENT_TURN:")) {
            String activeUser = message.substring(message.indexOf("CURRENT_TURN:") + 13).trim();
            if (activeUser.contains(";")) {
                activeUser = activeUser.split(";")[0].trim();
            }
            this.currentTurnPlayerName = activeUser;
            String cachedData = (String) rosterPanel.getClientProperty("raw_cache");
            updateVisualRoster(cachedData);
        }
        
        if (message.contains("wins") || message.contains("Congratulations") || message.contains("Dealer is playing")) {
            statusLabel.setText("<html><font color='yellow'>" + message + "</font></html>");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }
    }
}