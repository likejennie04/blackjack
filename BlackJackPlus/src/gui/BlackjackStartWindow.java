package gui;

import javax.swing.*;
import java.awt.*; 
import java.awt.event.*; 
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.net.URL; 

public class BlackjackStartWindow {    
    public BlackjackStartWindow() {
        
        JFrame frame = new JFrame("BlackJack+"); 
        frame.setSize(800, 600); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); 
        
        
        ImageIcon bgImage = new ImageIcon(getClass().getResource("/image/blackjackbackground.png")); 
        
        //label
        JLabel panel = new JLabel(bgImage); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        JLabel title = new JLabel("BLACKJACK+");
        title.setFont(new Font("Times New Roman", Font.BOLD, 50));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.PINK); 
        
        //button
        JButton offlineButton = new JButton("Offline");
        JButton onlineButton = new JButton("Online");
        JButton rulesButton = new JButton("Rules");
        JButton settingButton = new JButton("Settings"); 
        JButton exitButton = new JButton("Exit"); 
        
        JButton[] buttons = {offlineButton, onlineButton, rulesButton, settingButton, exitButton};
        Dimension buttonSize = new Dimension(200, 50);
        
        for (JButton btn : buttons) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT); 
            btn.setMaximumSize(buttonSize);
            btn.setFont(new Font("Times New Roman", Font.BOLD, 14));
        }
        JButton[] menuButtons = {onlineButton, offlineButton, rulesButton, settingButton, exitButton}; 
		for (JButton btn : menuButtons) {
			btn.addActionListener(e -> SoundManager.buttonOne()); 
		}
		
        
		//action
        offlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Debug: Entering Offline Mode...");
                new OfflineModeWindow(); 
                frame.dispose(); 
            }
        });
        
        onlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Debug: Entering Online Mode...");
                new OnlineModeWindow(); 
                frame.dispose(); 
            }
        });
        
        rulesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Debug: Opening Rules Window...");
                new RulesWindow(); 
                // Usually we don't dispose frame here so user can go back
            }
        });
        
        settingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Debug: Opening Settings Window...");
                new SettingWindow(); 
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Debug: Exiting game...");
                System.exit(0);
            }
        });
        
        panel.add(Box.createVerticalGlue());
        panel.add(title); 
        panel.add(Box.createRigidArea(new Dimension(0, 40)));
        panel.add(offlineButton); 
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(onlineButton); 
        panel.add(Box.createRigidArea(new Dimension(0, 20))); 
        panel.add(rulesButton); 
        panel.add(Box.createRigidArea(new Dimension(0, 20))); 
        panel.add(settingButton); 
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(exitButton);
        panel.add(Box.createVerticalGlue());
        
        frame.add(panel); 
        frame.setVisible(true);
    }

}