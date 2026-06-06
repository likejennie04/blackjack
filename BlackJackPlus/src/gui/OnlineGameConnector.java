package gui;

import javax.swing.*;
import backend.BlackjackClient;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;

/**
 * Re-engineered OnlineGameConnector.
 * Fully features localized option layout and synchronized Avatar resources.
 */
public class OnlineGameConnector {
    private JFrame frame;
    private BlackjackClient client;

    private JLabel statusLabel;
    private JPanel tablePanel; 
    private JPanel rosterPanel; 
    private JButton hitButton;
    private JButton standButton;
    private JButton returnButton;

    private String currentTurnPlayerName = "Waiting..."; 

    public OnlineGameConnector(BlackjackClient client) {
        this.client = client;
        
        // 🎯 修复点：移除了报错的 dispose() 链，改用反射强行预装载 AvatarManager
        try {
            if (AvatarManager.getAllAvatars().isEmpty()) {
                Class.forName("gui.AvatarManager");
            }
        } catch (Exception ignored) {}

        initializeGUI();
        startServerListener();
        
        // Synchronize initial visual request
        client.sendMove("REQUEST_SNAPSHOT");
    }

    private void initializeGUI() {
        frame = new JFrame("Blackjack+ Arena (Online Mode)");
        frame.setSize(900, 700); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 50, 30));

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
        tablePanel.setBackground(new Color(20, 50, 30));
        
        JScrollPane tableScroll = new JScrollPane(tablePanel);
        tableScroll.setBorder(null);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);

        hitButton = new JButton("HIT");
        standButton = new JButton("STAND");
        returnButton = new JButton("RETURN");

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
            executeQuitSequence();
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

    private JPanel createPlayerRowPanel(String name, String[] cards) {
        JPanel row = new JPanel(new BorderLayout()); 
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        row.setBackground(new Color(20, 50, 30)); 
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JPanel identityLabelPanel = new JPanel(new GridLayout(1, 1)); 
        identityLabelPanel.setOpaque(false); 
        identityLabelPanel.setPreferredSize(new Dimension(150, 0)); 
        
        JLabel nameTxT = new JLabel(name); 
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
            dealerBlock.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
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
                
                if (name.equalsIgnoreCase(client.getPlayerName().trim())) {
                    pNameLabel.setText(name + " (You)");
                    pNameLabel.setForeground(Color.GREEN);
                } else {
                    pNameLabel.setForeground(Color.WHITE);
                }

                if (name.equalsIgnoreCase(currentTurnPlayerName)) {
                    pBlock.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2, true));
                } else {
                    pBlock.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
                }

                pBlock.add(pAvatar, BorderLayout.CENTER);
                pBlock.add(pNameLabel, BorderLayout.SOUTH);
                rosterPanel.add(pBlock);
            }
        }
        rosterPanel.revalidate();
        rosterPanel.repaint();
    }

    private void executeQuitSequence() {
        client.disconnect();
        frame.dispose();
        new BlackjackStartWindow();
    }

    private void processServerMessage(String message) {
        System.out.println("[Server Data]: " + message);

        if (message.contains("TABLE_SNAPSHOT:")) {
            tablePanel.removeAll();
            String snapshotData = message.substring(message.indexOf("TABLE_SNAPSHOT:") + 15).trim();
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
            String turnData = message.replace("[Arena]:", "").trim();
            this.currentTurnPlayerName = turnData.substring(turnData.indexOf("CURRENT_TURN:") + 13).trim().replaceAll("\\s+", "");
            String cachedData = (String) rosterPanel.getClientProperty("raw_cache");
            if (cachedData != null) updateVisualRoster(cachedData);
        }
        
        if (message.contains("GAME_OVER_SUMMARY:") || message.contains("SHOW_FINAL_SUMMARY:")) {
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            
            String summaryText = message.replace("[Arena]:", "")
                                        .replace("GAME_OVER_SUMMARY:", "")
                                        .replace("SHOW_FINAL_SUMMARY:", "")
                                        .trim();
            
            String htmlSummary = "<html><font size='4' face='Times New Roman' color='black'>" 
                               + summaryText.replace("|", "<br>") + "</font></html>";
            
            String[] options = {"Restart Match", "Return to Menu"};
            
            int choice = JOptionPane.showOptionDialog(
                    frame, 
                    htmlSummary, 
                    "♣️ BLACKJACK+ ARENA SUMMARY ♣️", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE, 
                    null, 
                    options, 
                    options[0]
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                System.out.println("[Action]: Triggering multiplayer session recycle...");
                client.sendMove("START_COMMAND"); 
            } else {
                executeQuitSequence();
            }
        }
    }
}