package gui;

import javax.swing.*;
import java.awt.*; 
import java.awt.event.*; // For ActionListener
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.net.URL; 

public class BlackjackStartWindow {
<<<<<<< HEAD
    
   
    public static Clip backgroundMusic; 
    
    public BlackjackStartWindow() {
        
        /* Initialize the main start window */
        JFrame frame = new JFrame("BlackJack+"); 
        frame.setSize(800, 600); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); 
        
        /* Load and start background music */
        startBackgroundMusic("backgroundmusic.wav"); 
        
        /* Setup background image panel */
        ImageIcon bgImage = new ImageIcon(getClass().getResource("/image/blackjackbackground.png")); 
        JLabel panel = new JLabel(bgImage); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        
        /* Configure Game Title */
        JLabel title = new JLabel("BLACKJACK+");
        title.setFont(new Font("Times New Roman", Font.BOLD, 50));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.PINK); 
        
        /* Create main menu buttons */
        JButton offlineButton = new JButton("Offline");
        JButton onlineButton = new JButton("Online");
        JButton rulesButton = new JButton("Rules");
        JButton settingButton = new JButton("Settings"); 
        JButton exitButton = new JButton("Exit"); 
        
        /* Group buttons to apply same style */
        JButton[] buttons = {offlineButton, onlineButton, rulesButton, settingButton, exitButton};
        Dimension buttonSize = new Dimension(200, 50);
        
        for (JButton btn : buttons) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT); 
            btn.setForeground(Color.PINK); 
            btn.setMaximumSize(buttonSize);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
        }
        
        /* --- Define Button Actions (Student Style: Anonymous Inner Classes) --- */
        
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
        
        /* Add components to the layout with spacing (glue and rigid areas) */
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
        
        /* Finalize frame setup */
        frame.add(panel); 
        frame.setVisible(true);
    }
    
    /**
     * Logic to play or pause the music.
     * Can be called statically from SettingWindow.
     */
    public static void toggleMusic() {
        if (backgroundMusic != null) {
            if (backgroundMusic.isRunning()) {
                backgroundMusic.stop(); 
                System.out.println("Music Paused");
            } else {
                backgroundMusic.start(); 
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Music Started");
            }
        }
    }
    
    /**
     * Helper method to load and play wav file.
     */
    private void startBackgroundMusic(String musicFileName) {
        try {
            URL url = getClass().getResource("/sound/" + musicFileName); 
            
            if (url == null) {
                System.err.println("Error: Could not find music file at /sound/" + musicFileName); 
                return; 
            }
            
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url); 
            backgroundMusic = AudioSystem.getClip(); 
            backgroundMusic.open(audioIn); 
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); 
            
        } catch (Exception e) {
            System.err.println("Error initializing background music."); 
            e.printStackTrace(); 
        }
    }
=======
	
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
>>>>>>> 9bc70f520980f26280c4ee767a37958619a7beca
}