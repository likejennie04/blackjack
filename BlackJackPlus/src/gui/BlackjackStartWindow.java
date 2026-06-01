package gui;

import javax.swing.*;
import java.awt.*; 
import java.awt.event.*; 
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip;
import java.net.URL; 

public class BlackjackStartWindow {
    
    public static Clip backgroundMusic; 
    
    public BlackjackStartWindow() {
        /* 1. 初始化主窗口 */
        JFrame frame = new JFrame("BlackJack+"); 
        frame.setSize(800, 600); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); 
        
        /* 2. 启动背景音乐 */
        startBackgroundMusic("backgroundmusic.wav"); 
        
        /* 3. 设置背景面板 */
        ImageIcon bgImage = new ImageIcon(getClass().getResource("/image/blackjackbackground.png")); 
        JLabel panel = new JLabel(bgImage); 
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        
        /* 4. 游戏标题 */
        JLabel title = new JLabel("BLACKJACK+");
        title.setFont(new Font("Times New Roman", Font.BOLD, 50));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.PINK); 
        
        /* 5. 创建按钮 */
        JButton offlineButton = new JButton("Offline");
        JButton onlineButton = new JButton("Online");
        JButton rulesButton = new JButton("Rules");
        JButton settingButton = new JButton("Settings"); 
        JButton exitButton = new JButton("Exit"); 
        
        JButton[] buttons = {offlineButton, onlineButton, rulesButton, settingButton, exitButton};
        Dimension buttonSize = new Dimension(200, 50);
        
        for (JButton btn : buttons) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT); 
            btn.setForeground(Color.PINK); 
            btn.setMaximumSize(buttonSize);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            // 整合音效逻辑 (如果你的项目中包含 SoundManager)
            btn.addActionListener(e -> {
                try { SoundManager.buttonOne(); } catch (Exception ex) {}
            });
        }
        
        /* 6. 按钮事件监听 */
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
        
        /* 7. 布局排版 */
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
}