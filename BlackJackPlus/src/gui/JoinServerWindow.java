package gui;

import javax.swing.*;
import backend.BlackjackClient; 
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;

public class JoinServerWindow {
    private JFrame frame;
    private JTextField nameField; 
    private JTextField ipField; 
    private int currentAvatarIndex = 0;

    // --- UI Panels for Lobby Transition ---
    private JPanel inputPanel;   // The original credentials input view
    private JPanel lobbyPanel;   // The new "Waiting for Host" lobby view
    private JLabel lobbyStatusLabel;

    public JoinServerWindow() {
        frame = new JFrame("Join Server"); 
        frame.setSize(800, 650); 
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Setup CardLayout or switchable panels natively
        frame.setLayout(new CardLayout());

        initializeInputPanel();
        initializeLobbyPanel();

        frame.add(inputPanel, "INPUT");
        frame.add(lobbyPanel, "LOBBY");

        // Show input view first
        CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), "INPUT");

        frame.setVisible(true);
    }

    /**
     * Renders the traditional name and IP parameter entry components.
     */
    private void initializeInputPanel() {
        inputPanel = new JPanel(); 
        inputPanel.setBackground(new Color(20, 50, 30));
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS)); 
        
        JLabel title = new JLabel("JOIN MULTIPLAYER");
        title.setFont(new Font("Times New Roman", Font.BOLD, 24));
        title.setForeground(Color.PINK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT); 

        // Avatar Area
        JLabel avatarLabel = new JLabel();
        if (!AvatarManager.getAllAvatars().isEmpty()) {
            avatarLabel.setIcon(AvatarManager.getAllAvatars().get(0));
        }
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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

        JLabel hintLabel = new JLabel("(Click Photo to Change Avatar)");
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel("Enter Your Name: "); 
        nameLabel.setForeground(Color.LIGHT_GRAY);
        nameLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        nameField = new JTextField("Player_" + (int)(Math.random() * 900 + 100), 15); 
        nameField.setMaximumSize(new Dimension(200, 30));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel promptLabel = new JLabel("Enter Host IP Address: "); 
        promptLabel.setForeground(Color.LIGHT_GRAY);
        promptLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        ipField = new JTextField("localhost", 15); 
        ipField.setMaximumSize(new Dimension(200, 30));
        ipField.setHorizontalAlignment(JTextField.CENTER);
        ipField.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton joinButton = new JButton("Join Game"); 
        JButton returnButton = new JButton("Return"); 
        
        joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        joinButton.addActionListener(e -> {
            SoundManager.buttonTwo();
            String targetIp = ipField.getText().trim(); 
            String playerName = nameField.getText().trim(); 
            
            try {
                BlackjackClient client = new BlackjackClient(targetIp); 
                client.setAvatarId(currentAvatarIndex);
                client.setPlayerName(playerName); 
                
                // Register identity with the central handler mapping
                client.sendMove("NAME_REGISTER:" + playerName);
                
               
                CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
                cl.show(frame.getContentPane(), "LOBBY");
                
               
                startLobbyNetworkListener(client);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Could not connect to server at " + targetIp,
                    "Connection Error", 
                    JOptionPane.ERROR_MESSAGE); 
                ex.printStackTrace();
            }
        });
        
        returnButton.addActionListener(e -> {
            SoundManager.buttonOne();
            frame.dispose(); 
            new BlackjackStartWindow(); 
        });
        
        inputPanel.add(Box.createVerticalGlue());
        inputPanel.add(title); 
        inputPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        inputPanel.add(avatarLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        inputPanel.add(hintLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        inputPanel.add(nameLabel); 
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        inputPanel.add(nameField); 
        inputPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        inputPanel.add(promptLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5))); 
        inputPanel.add(ipField); 
        inputPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        inputPanel.add(joinButton); 
        inputPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        inputPanel.add(returnButton); 
        inputPanel.add(Box.createVerticalGlue()); 
    }

    
    private void initializeLobbyPanel() {
        lobbyPanel = new JPanel();
        lobbyPanel.setBackground(new Color(15, 35, 25)); // Slightly darker green for lobby
        lobbyPanel.setLayout(new BoxLayout(lobbyPanel, BoxLayout.Y_AXIS));

        JLabel lobbyTitle = new JLabel("MULTIPLAYER LOBBY");
        lobbyTitle.setFont(new Font("Times New Roman", Font.BOLD, 22));
        lobbyTitle.setForeground(Color.YELLOW);
        lobbyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lobbyStatusLabel = new JLabel("Successfully Connected! Waiting for Host to start game...", JLabel.CENTER);
        lobbyStatusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        lobbyStatusLabel.setForeground(Color.WHITE);
        lobbyStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // A simple visual loading indicator using standard swing typography animation hooks
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new Dimension(300, 15));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lobbyPanel.add(Box.createVerticalGlue());
        lobbyPanel.add(lobbyTitle);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        lobbyPanel.add(lobbyStatusLabel);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        lobbyPanel.add(progressBar);
        lobbyPanel.add(Box.createVerticalGlue());
    }

   
    private void startLobbyNetworkListener(BlackjackClient client) {
        new Thread(() -> {
            try {
                BufferedReader in = client.getInputStream();
                String serverMessage;
                // Keep reading packets silently in the background thread
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println("[Lobby Data Stream]: " + serverMessage);
                    
                    // The magic signal dispatched by Host Server when clicking "START"
                    if (serverMessage.contains("GAME_STARTED")) {
                        // Deliver transition to Event Dispatch Thread immediately
                        SwingUtilities.invokeLater(() -> {
                            // Perfect timing: Launch Arena game table interface now
                            new OnlineGameConnector(client);
                            // Close the current transition lobby window safely
                            frame.dispose();
                        });
                        break; // Kill this temporary lobby thread safely
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    lobbyStatusLabel.setText("<html><font color='red'>Connection to host failed.</font></html>");
                });
            }
        }).start();
    }
}