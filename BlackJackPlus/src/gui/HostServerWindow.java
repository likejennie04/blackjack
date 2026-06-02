package gui;

import javax.swing.*;
import backend.BlackjackServer;
import backend.BlackjackClient; 
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HostServerWindow {
    private JFrame frame;
    private JButton startServerButton;
    private JButton startGameButton; 
    private JLabel statusInfoLabel; 
    private JLabel avatarLabel; 
    private BlackjackClient hostClient; 
    private int currentAvatarIndex = 0; 

    public HostServerWindow() {
        frame = new JFrame("Host Server - Setup"); 
        frame.setSize(800, 650); 
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel(); 
        panel.setBackground(new Color(20, 50, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        
        JLabel title = new JLabel("HOST MULTIPLAYER");
        title.setFont(new Font("Times New Roman", Font.BOLD, 24));
        title.setForeground(Color.PINK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT); 

        // Avatar Selection Area
        avatarLabel = new JLabel();
        if (!AvatarManager.getAllAvatars().isEmpty()) {
            avatarLabel.setIcon(AvatarManager.getAllAvatars().get(0));
        }
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Click to cycle avatars
        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!AvatarManager.getAllAvatars().isEmpty()) {
                    currentAvatarIndex = (currentAvatarIndex + 1) % AvatarManager.getAllAvatars().size();
                    avatarLabel.setIcon(AvatarManager.getAllAvatars().get(currentAvatarIndex));
                    SoundManager.buttonOne();
                }
            }
        });

        JLabel avatarHint = new JLabel("(Click to choose Host Avatar)");
        avatarHint.setForeground(Color.GRAY);
        avatarHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusInfoLabel = new JLabel("Server Status: Offline", JLabel.CENTER); 
        statusInfoLabel.setForeground(Color.LIGHT_GRAY);
        statusInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        startServerButton = new JButton("1. Start Server & Set Name"); 
        startGameButton = new JButton("2. Start Game (Deal Cards)"); 
        startGameButton.setEnabled(false); 
        
        JButton returnButton = new JButton("Return");
        
        startServerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Step 1: Initialize server and host identity
        startServerButton.addActionListener(e -> {
            SoundManager.buttonOne();
            
            String hostName = JOptionPane.showInputDialog(frame, "What is your name, Host?", "Host Identity", JOptionPane.QUESTION_MESSAGE);
            if (hostName == null || hostName.trim().isEmpty()) hostName = "Host_Player";
            final String finalName = hostName;

            // Start server in background thread
            new Thread(() -> {
                try {
                    BlackjackServer.main(null); 
                } catch(Exception ex) {
                    ex.printStackTrace(); 
                }
            }).start(); 
            
            try { Thread.sleep(600); } catch (InterruptedException ex) {}
            
            try {
                // Connect host as a local client
                hostClient = new BlackjackClient("localhost"); 
                hostClient.setAvatarId(currentAvatarIndex);
                hostClient.sendMove("NAME_REGISTER:" + finalName);
                
                startServerButton.setEnabled(false); 
                startServerButton.setText("Server Running as: " + finalName);
                statusInfoLabel.setText("Server Online! Wait for players to join...");
                startGameButton.setEnabled(true); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });
        
        // Step 2: Transition to Game Arena
        startGameButton.addActionListener(e -> {
            SoundManager.buttonTwo();
            if (hostClient != null) {
                hostClient.sendMove("START_COMMAND"); 
                new OnlineGameConnector(hostClient); 
                frame.dispose();
            }
        });
        
        returnButton.addActionListener(e -> {
            SoundManager.buttonOne();
            // Disconnect if server was already started
            if (hostClient != null) {
                hostClient.disconnect();
            }
            frame.dispose();
            new BlackjackStartWindow(); 
        });
        
        // Layout components
        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(avatarLabel);
        panel.add(avatarHint);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(statusInfoLabel); 
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(startServerButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(startGameButton); 
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(returnButton); 
        panel.add(Box.createVerticalGlue()); 
        
        frame.add(panel); 
        frame.setVisible(true);
    }
}