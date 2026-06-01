package gui;
import javax.swing.*;
import java.awt.*; 
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.net.URL; 

public class BlackjackStartWindow {
	
		public BlackjackStartWindow() {
			
			//start window
			JFrame frame = new JFrame("BlackJack+"); 
			frame.setSize(800, 600); 
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null); 
			
			
			//main panel
			ImageIcon bgImage = new ImageIcon(getClass().getResource("/image/blackjackbackground.png")); 
			
			JLabel panel = new JLabel(bgImage); 
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
			
			JLabel title = new JLabel("BLACKJACK+");
			title.setFont(new Font("Times New Roman", Font.BOLD, 50));
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			title.setForeground(Color.PINK); 
			
			
			//buttons
			JButton onlineButton = new JButton("Online"); 
			JButton offlineButton = new JButton("Offline");
			JButton rulesButton = new JButton("Rules");
			JButton exitButton = new JButton("Exit"); 
			JButton settingButton = new JButton("Settings"); 
			
			onlineButton.setAlignmentX(Component.CENTER_ALIGNMENT); 
			offlineButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			rulesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			exitButton.setAlignmentX(Component.CENTER_ALIGNMENT); 
			settingButton.setAlignmentX(Component.CENTER_ALIGNMENT); 
			
			onlineButton.setForeground(Color.PINK); 
			offlineButton.setForeground(Color.PINK);
			rulesButton.setForeground(Color.PINK);
			exitButton.setForeground(Color.PINK);
			settingButton.setForeground(Color.PINK); 
			
			Dimension buttonSize = new Dimension(200, 50); 
			
			onlineButton.setMaximumSize(buttonSize);
			offlineButton.setMaximumSize(buttonSize); 
			rulesButton.setMaximumSize(buttonSize); 
			exitButton.setMaximumSize(buttonSize);
			settingButton.setMaximumSize(buttonSize);
			
			//add sounds to buttons
			JButton[] menuButtons = {onlineButton, offlineButton, rulesButton, settingButton, exitButton}; 
			for (JButton btn : menuButtons) {
				btn.addActionListener(e -> SoundManager.buttonOne()); 
			}
			
			//action
			offlineButton.addActionListener(e -> {
				System.out.println("Offline Mode Chosen"); 
				new OfflineModeWindow(); 
				frame.dispose(); 
			});
			
			onlineButton.addActionListener(e -> {
				System.out.println("Online Mode Chosen");
				new OnlineModeWindow(); 
				frame.dispose(); 
			});
			
			rulesButton.addActionListener(e -> {
                 System.out.println("BlackJack+ Rules"); 
                 new RulesWindow(); 
                 frame.dispose(); 
			});
			
			exitButton.addActionListener( e ->
				System.exit(0));
			
			settingButton.addActionListener( e -> {
				System.out.println("Setting clicked"); 
				new SettingWindow(); 
				frame.dispose(); 
			});
			
			panel.add(Box.createVerticalGlue());
			panel.add(title); 
			panel.add(Box.createRigidArea(new Dimension(0,40)));
			panel.add(offlineButton); 
			panel.add(Box.createRigidArea(new Dimension(0,20)));
			panel.add(onlineButton); 
			panel.add(Box.createRigidArea(new Dimension(0,20))); 
			panel.add(rulesButton); 
			panel.add(Box.createRigidArea(new Dimension(0, 20))); 
			panel.add(settingButton); 
			panel.add(Box.createRigidArea(new Dimension(0,20)));
			panel.add(exitButton);
			panel.add(Box.createVerticalGlue());
			
			
			frame.add(panel); 
			
			frame.setVisible(true);
		}
}