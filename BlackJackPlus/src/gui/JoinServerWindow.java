package gui;

import javax.swing.*;
import backend.BlackjackClient; 
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JoinServerWindow {
    private JFrame frame;
    private JTextField nameField; 
    private JTextField ipField; 
    private int currentAvatarIndex = 0;

    public JoinServerWindow() {
        frame = new JFrame("Join Server"); 
        frame.setSize(800, 650); 
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel(); 
        panel.setBackground(new Color(20, 50, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        
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
        
        nameField = new JTextField("Player_" + (int)(Math.random() * 900 + 100), 15); // 默认随机名如 Player_342
        nameField.setMaximumSize(new Dimension(200, 30));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // IP Address Area
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
            SoundManager.buttonOne(); 
            String targetIp = ipField.getText().trim(); 
            String playerName = nameField.getText().trim(); 
            
            try {
                BlackjackClient client = new BlackjackClient(targetIp); 
                client.setAvatarId(currentAvatarIndex);
                client.setPlayerName(playerName); 
                
                
                client.sendMove("NAME_REGISTER:" + playerName);
                
                new OnlineGameConnector(client); 
                frame.dispose(); 
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
        
        // Add to layout
        panel.add(Box.createVerticalGlue());
        panel.add(title); 
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(avatarLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(hintLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        panel.add(nameLabel); // 👈 放入界面
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(nameField); 
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        panel.add(promptLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); 
        panel.add(ipField); 
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(joinButton); 
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(returnButton); 
        panel.add(Box.createVerticalGlue()); 
        
        frame.add(panel);
        frame.setVisible(true);
    }
}