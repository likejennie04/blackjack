package gui;

import javax.swing.*;
import backend.BlackjackServer;
import backend.BlackjackClient; 
import java.awt.*;

public class HostServerWindow {
    private JFrame frame;
    private JButton startServerButton;
    private JButton startGameButton; 
    private BlackjackClient hostClient; 

    public HostServerWindow() {
        frame = new JFrame("Host Server"); 
        frame.setSize(800, 600); 
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel(); 
        panel.setBackground(new Color(20, 50, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        
        JLabel title = new JLabel("HOST MULTIPLAYER");
        title.setFont(new Font("Times New Roman", Font.BOLD, 24));
        title.setForeground(Color.PINK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        JLabel info = new JLabel("Server will run on port 8888"); 
        info.setForeground(Color.LIGHT_GRAY);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        startServerButton = new JButton("1. Start Server"); 
        startGameButton = new JButton("2. Start Game (Deal Cards)"); 
        startGameButton.setEnabled(false); 
        
        JButton returnButton = new JButton("Return");
        
        startServerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // step 1 : start the server
        startServerButton.addActionListener(e -> {
            SoundManager.buttonOne();
            new Thread(() -> {
                try {
                    BlackjackServer.main(null); 
                } catch(Exception ex) {
                    ex.printStackTrace(); 
                }
            }).start(); 
            
            try { Thread.sleep(500); } catch (InterruptedException ex) {}
            
            try {
                hostClient = new BlackjackClient("localhost"); 
                startServerButton.setEnabled(false); // 防止重复启动
                startServerButton.setText("Server Running...");
                startGameButton.setEnabled(true); // 👈 激活开始游戏按钮
                JOptionPane.showMessageDialog(frame, "Server is live! Wait for friends to join, then click 'Start Game'.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });
        
        // step 2 : get the card
        startGameButton.addActionListener(e -> {
            SoundManager.buttonOne();
            if (hostClient != null) {
                
                hostClient.sendMove("START_COMMAND"); 
                
                
                new OnlineGameConnector(hostClient); 
                frame.dispose();
            }
        });
        
        returnButton.addActionListener(e -> {
            SoundManager.buttonOne();
            frame.dispose();
            new BlackjackStartWindow(); 
        });
        
        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(info); 
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(startServerButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(startGameButton); // 👈 加入界面
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(returnButton); 
        panel.add(Box.createVerticalGlue()); 
        
        frame.add(panel); 
        frame.setVisible(true);
    }
}