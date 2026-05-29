package gui;
import javax.swing.*;
import java.awt.*; 
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 
import java.net.URL; 

public class BlackjackStartWindow {
	
		private Clip backgroundMusic; 
		
		public BlackjackStartWindow() {
			
			//start window
			JFrame frame = new JFrame("BlackJack+"); 
			frame.setSize(800, 600); 
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null); 
			
			//background music 
			startBackgroundMusic("backgroundmusic.wav"); 
			
			//main panel
			ImageIcon bgImage = new ImageIcon(getClass().getResource("/image/blackjackbackground.png")); 
			
			JLabel panel = new JLabel(bgImage); 
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
			
			JLabel title = new JLabel("BLACKJACK+");
			title.setFont(new Font("Times New Roman", Font.BOLD, 50));
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			title.setForeground(Color.PINK); 
			
			JButton startButton = new JButton("Start Game"); 
			JButton rulesButton = new JButton("Rules");
			JButton exitButton = new JButton("Exit"); 
			JButton settingButton = new JButton("Settings"); 
			
			startButton.setAlignmentX(Component.CENTER_ALIGNMENT); 
			rulesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			exitButton.setAlignmentX(Component.CENTER_ALIGNMENT); 
			settingButton.setAlignmentX(Component.CENTER_ALIGNMENT); 
			
			startButton.setForeground(Color.PINK); 
			rulesButton.setForeground(Color.PINK);
			exitButton.setForeground(Color.PINK);
			settingButton.setForeground(Color.PINK); 
			
			Dimension buttonSize = new Dimension(200, 50); 
			
			startButton.setMaximumSize(buttonSize);
			rulesButton.setMaximumSize(buttonSize); 
			exitButton.setMaximumSize(buttonSize);
			settingButton.setMaximumSize(buttonSize);
			
			//action
			startButton.addActionListener(e -> {
				System.out.println("BlackJack Start Button clicked"); 
				new PlayerWindow(); 
				frame.dispose(); 
			});
			
			rulesButton.addActionListener(e -> {
                 System.out.println("BlackJack+ Rules"); 
                 new RulesWindow(); 
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
			panel.add(startButton); 
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
		private void startBackgroundMusic(String musicFileName) {
			try {
				URL url = getClass().getResource("/sound/" + musicFileName); 
				
				if (url == null) {
					System.err.println("Could not find music file: " + musicFileName); 
					return; 
				}
				
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(url); 
				backgroundMusic = AudioSystem.getClip(); 
				backgroundMusic.open(audioIn); 
				
				backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); 
			} catch (Exception e) {
				System.err.println("Erro initializing background music."); 
				e.printStackTrace(); 
			}
		}

}

		
	