package gui;

import javax.swing.*;
import backend.BlackjackClient;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import backend.Hand; 
import backend.Card; 

public class OnlineGameConnector {
    private JFrame frame;
    private BlackjackClient client;

    // --- Core Containers ---
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JPanel tablePanel; 
    private JPanel rosterPanel; 
    private JButton hitButton;
    private JButton standButton;
    private JButton settingButton; 
    private JButton returnButton;
    
    // Global reference to clear the popup automatically when host restarts
    private JFrame resultWindow; 

    private String currentTurnPlayerName = "Waiting..."; 

    public OnlineGameConnector(BlackjackClient client) {
        this.client = client;
        
        try {
            if (AvatarManager.getAllAvatars().isEmpty()) {
                Class.forName("gui.AvatarManager");
            }
        } catch (Exception ignored) {}

        initializeGUI();
        startServerListener();
        
        client.sendMove("REQUEST_SNAPSHOT");
    }

    private void initializeGUI() {
        frame = new JFrame("Blackjack+ Arena (Online Mode)");
        frame.setSize(Config.windowWidth > 0 ? Config.windowWidth : 900, 
                      Config.windowHeight > 0 ? Config.windowHeight : 700); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Config.tableColor != null ? Config.tableColor : new Color(20, 50, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        statusLabel = new JLabel("Game Started! Good Luck!", JLabel.CENTER);
        statusLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        statusLabel.setForeground(new Color(220, 220, 100));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        rosterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        rosterPanel.setOpaque(false);
        rosterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 50)));

        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(rosterPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Config.tableColor != null ? Config.tableColor : new Color(20, 50, 30));
        
        JScrollPane tableScroll = new JScrollPane(tablePanel);
        tableScroll.setBorder(null);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);

        hitButton = new JButton("HIT");
        standButton = new JButton("STAND");
        settingButton = new JButton("SETTINGS");
        returnButton = new JButton("RETURN");
        
        styleActionButton(hitButton, new Color(180, 40, 40));
        styleActionButton(standButton, new Color(40, 100, 180));
        styleActionButton(settingButton, new Color(100, 100, 100));
        styleActionButton(returnButton, new Color(100, 100, 100));        

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
        
        settingButton.addActionListener(e -> {
            SoundManager.buttonOne();
            new SettingWindow(); 
        });

        returnButton.addActionListener(e -> {
            SoundManager.buttonOne();
            executeQuitSequence();
        });

        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        controlPanel.add(settingButton);
        controlPanel.add(returnButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void styleActionButton(JButton button, Color bg) {
        button.setPreferredSize(new Dimension(120, 40)); 
        button.setBackground(bg); 
        button.setForeground(Color.WHITE); 
        button.setFont(new Font("Times New Roman", Font.BOLD, 14)); 
        button.setFocusPainted(false); 
        button.setBorderPainted(false); 
        button.setOpaque(true); 
    }

    public void updateTheme() {
        if (mainPanel != null && tablePanel != null) {
            mainPanel.setBackground(Config.tableColor);
            tablePanel.setBackground(Config.tableColor);
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    public void applyResolution(int width, int height) {
        if (frame != null) {
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
        }
    }

    private int getScoreUsingHand(String[] cards) {
        Hand hand = new Hand();

        for (String cardCode : cards) {
            cardCode = cardCode.trim().toLowerCase();
            if (cardCode.isEmpty() || cardCode.contains("hidden")) {
                continue;
            }

            if (cardCode.length() < 2) continue;

            String rankPart = cardCode.substring(0, cardCode.length() - 1);
            char suitPart = cardCode.charAt(cardCode.length() - 1);

            int rank;
            switch (rankPart) {
                case "a":  rank = 1;  break;
                case "j":  rank = 11; break;
                case "q":  rank = 12; break;
                case "k":  rank = 13; break;
                default:
                    try {
                        rank = Integer.parseInt(rankPart);
                    } catch (NumberFormatException e) {
                        rank = 1;
                    }
                    break;
            }

            int suit;
            switch (suitPart) {
                case 'd': suit = 0; break;
                case 'h': suit = 1; break;
                case 'c': suit = 2; break;
                case 's': suit = 3; break;
                default:  suit = -1; break;
            }

            hand.addCard(new Card(rank, suit));
        }

        return hand.getScore();
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

    private JPanel createPlayerRowPanel(String name, String[] cards) {
        JPanel row = new JPanel(new BorderLayout()); 
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        row.setBackground(Config.tableColor != null ? Config.tableColor : new Color(20, 50, 30)); 
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JPanel identityLabelPanel = new JPanel(new GridLayout(1, 1)); 
        identityLabelPanel.setOpaque(false); 
        identityLabelPanel.setPreferredSize(new Dimension(150, 0)); 
        
        String displayName = name.trim();
        String cleanParam = displayName.toLowerCase();
        String cleanLocal = client.getPlayerName().trim().toLowerCase();

        if (cleanParam.equals(cleanLocal) || (cleanParam.equals("1") && cleanLocal.equals("anonymous"))) {
            if (!displayName.contains("(You)")) {
                displayName = displayName + " (You)";
            }
        }

        int score = getScoreUsingHand(cards);
        JLabel nameTxT = new JLabel(displayName + " (" + score + ")");
        nameTxT.setFont(new Font("Times New Roman", Font.BOLD, 15)); 
        nameTxT.setForeground(Color.WHITE);
        identityLabelPanel.add(nameTxT);
        row.add(identityLabelPanel, BorderLayout.WEST); 
        
        JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        handPanel.setOpaque(false);
        
        for (String card : cards) {
            if (!card.trim().isEmpty()) {
                displayCard(handPanel, card.trim());
            }
        }
        row.add(handPanel, BorderLayout.CENTER); 
        return row; 
    }

    private URL getCardImageURL(String cardCode) {
        cardCode = cardCode.trim().toLowerCase().replace("[", "").replace("]", "");
        if (cardCode.contains("hidden") || cardCode.isEmpty()) {
            return getClass().getResource("/image/card_back.png");
        }

        if (cardCode.length() < 2) return null;

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
            cardShell.setPreferredSize(new Dimension(65, 90));
            cardShell.setBackground(Color.WHITE);
            cardShell.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 1, true));
            
            Image scaled = icon.getImage().getScaledInstance(55, 80, Image.SCALE_SMOOTH);
            JLabel cardLabel = new JLabel(new ImageIcon(scaled));
            
            cardShell.add(cardLabel, BorderLayout.CENTER);
            panel.add(cardShell);
        } else {
            JLabel errorLabel = new JLabel("[" + cardCode + "]");
            errorLabel.setForeground(Color.YELLOW);
            panel.add(errorLabel);
        }
    }

    private void updateVisualRoster(String rawRosterData) {
        rosterPanel.removeAll();

        JPanel dealerBlock = new JPanel(new BorderLayout());
        dealerBlock.setOpaque(false);
        JLabel dealerAvatar = new JLabel();
        
        if (AvatarManager.getAllAvatars() != null && !AvatarManager.getAllAvatars().isEmpty()) {
            dealerAvatar.setIcon(AvatarManager.getAllAvatars().get(0)); 
        }
        dealerAvatar.setHorizontalAlignment(JLabel.CENTER);
        JLabel dealerNameLabel = new JLabel("Dealer", JLabel.CENTER);
        dealerNameLabel.setFont(new Font("Times New Roman", Font.BOLD, 12));
        dealerBlock.add(dealerAvatar, BorderLayout.CENTER);
        dealerBlock.add(dealerNameLabel, BorderLayout.SOUTH);
        
        if (currentTurnPlayerName.equalsIgnoreCase("Dealer")) {
            dealerNameLabel.setForeground(Color.YELLOW);
            dealerBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2, true));
        } else {
            dealerNameLabel.setForeground(Color.RED);
            dealerBlock.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }
        rosterPanel.add(dealerBlock);

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
                if (AvatarManager.getAllAvatars() != null && !AvatarManager.getAllAvatars().isEmpty()) {
                    int safeId = (avatarId >= 0 && avatarId < AvatarManager.getAllAvatars().size()) ? avatarId : 0;
                    pAvatar.setIcon(AvatarManager.getAllAvatars().get(safeId));
                }
                pAvatar.setHorizontalAlignment(JLabel.CENTER);
                
                JLabel pNameLabel = new JLabel(name, JLabel.CENTER);
                pNameLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
                
                String cleanName = name.toLowerCase();
                String cleanLocalName = client.getPlayerName().trim().toLowerCase();

                if (cleanName.equals(cleanLocalName) || (cleanName.equals("1") && cleanLocalName.equals("anonymous"))) {
                    pNameLabel.setText(name + " (You)");
                    pNameLabel.setForeground(Color.GREEN);
                } else {
                    pNameLabel.setForeground(Color.WHITE);
                }

                if (name.equalsIgnoreCase(currentTurnPlayerName)) {
                    pBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2, true));
                } else {
                    pBlock.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                }

                pBlock.add(pAvatar, BorderLayout.CENTER);
                pBlock.add(pNameLabel, BorderLayout.SOUTH);
                rosterPanel.add(pBlock);
            }
        }
        rosterPanel.revalidate();
        rosterPanel.repaint();
    }
    public void setResultWindow(JFrame window) {
    	this.resultWindow = window; 
    }

    private void executeQuitSequence() {
        client.disconnect();
        frame.dispose();
        if (resultWindow != null) {
            resultWindow.dispose();
        }
        new BlackjackStartWindow();
    }

    private void processServerMessage(String message) {
        System.out.println("[Server Data]: " + message);

        // --- AUTOMATIC RESTART TRIGGER ---
        // Clears old configurations when the host initiates a new match session
        if (message.contains("GAME_STARTED") || message.contains("ROUND_START") || message.contains("NEW_ROUND")) {
            statusLabel.setText("New Round Started! Good Luck!");
            
            // Automatically close the popup window across all players
            if (resultWindow != null) {
                resultWindow.dispose();
                resultWindow = null;
            }
            tablePanel.removeAll();
            tablePanel.revalidate();
            tablePanel.repaint();
        }

        if (message.contains("TABLE_SNAPSHOT:")) {
            String snapshotData = message.substring(message.indexOf("TABLE_SNAPSHOT:") + 15).trim();
            tablePanel.putClientProperty("latest_snapshot_raw", snapshotData);

            tablePanel.removeAll();
            String[] rows = snapshotData.split(";");
            for (String row : rows) {
                if (row.trim().isEmpty()) continue;
                String[] tokens = row.split("=");
                if (tokens.length < 2) continue;
                String name = tokens[0].trim();
                String[] cards = tokens[1].split(",");
                tablePanel.add(createPlayerRowPanel(name, cards));
                tablePanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            tablePanel.revalidate();
            tablePanel.repaint();
        }
        

        if (message.contains("ROSTER_UPDATE:")) {
            String data = message.substring(message.indexOf("ROSTER_UPDATE:") + 14).trim();
            rosterPanel.putClientProperty("raw_cache", data);
            updateVisualRoster(data);
        }
        
        if (message.contains("WAIT_FOR_HOST")) {
            statusLabel.setText("Waiting for host to restart the game...");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }
        if (message.contains("YOU_ARE_HOST")) {
            statusLabel.setText("You are the host.");
        }

        if (message.contains("UNLOCK_ACTIONS_FOR_CLIENT")) {
            statusLabel.setText("★ Your Turn! Make your move. ★");
            hitButton.setEnabled(true);
            standButton.setEnabled(true);
        } 
        else if (message.contains("LOCK_ACTIONS_FOR_CLIENT")) {
            statusLabel.setText("Waiting for other players...");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }
        
        if (message.contains("CURRENT_TURN:")) {
            int index = message.indexOf("CURRENT_TURN:");
            this.currentTurnPlayerName = message.substring(index + 13).trim();
            
            String cachedData = (String) rosterPanel.getClientProperty("raw_cache");
            if (cachedData != null) {
                updateVisualRoster(cachedData);
            }
        }
     // --- AUTOMATIC RESTART TRIGGER ---
        if (message.contains("GAME_STARTED") || message.contains("ROUND_START") || message.contains("NEW_ROUND")) {
            statusLabel.setText("New Round Started! Good Luck!");
            
        }
        
        if (message.contains("GAME_OVER_SUMMARY:") || message.contains("SHOW_FINAL_SUMMARY:")) {
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            
            String cachedSnapshot = (String) tablePanel.getClientProperty("latest_snapshot_raw");
            if (cachedSnapshot == null) {
                cachedSnapshot = "Dealer=10s,8c;YourName=Kc,Ah";
            }
            FinalBoardPrint.showOnlineSummary(frame, cachedSnapshot, client, this);
        }
    }
}