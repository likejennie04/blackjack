package gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*; 

public class SettingWindow {
    public SettingWindow() {
       
        final JFrame frame = new JFrame("Game Settings");
        frame.setSize(400, 500); 
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Config.tableColor);

       
        JLabel titleLabel = new JLabel("SETTING MENU");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        String initialMusicText = SoundManager.isMusicPlaying() ? "Music ON" : "Music OFF"; 
        
        final JButton musicBtn = new JButton(initialMusicText);
        musicBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        musicBtn.setForeground(Color.PINK);
        musicBtn.setMaximumSize(new Dimension(200, 40));
        musicBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.buttonOne(); // Button Click SFX
                SoundManager.toggleMusic(); // Toggle background loop
                
                if (SoundManager.isMusicPlaying()) {
                    musicBtn.setText("Music ON");
                } else {
                    musicBtn.setText("Music OFF");
                }
            }
        });

        // --- 3. Color Selection ---
        JLabel colorLabel = new JLabel("Change Table Theme:");
        colorLabel.setForeground(Color.WHITE);
        colorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] options = {"Classic Green", "Deep Blue", "Midnight Red"};
        final JComboBox colorBox = new JComboBox(options);
        colorBox.setMaximumSize(new Dimension(200, 40));
        colorBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (Config.tableColor.equals(new Color(15, 30, 80))) colorBox.setSelectedIndex(1);
        else if (Config.tableColor.equals(new Color(80, 10, 10))) colorBox.setSelectedIndex(2);

        colorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.buttonOne(); // Play click sound when selector changes
                
                String selected = (String) colorBox.getSelectedItem();
                if (selected.equals("Deep Blue")) {
                    Config.tableColor = new Color(15, 30, 80);
                } else if (selected.equals("Midnight Red")) {
                    Config.tableColor = new Color(80, 10, 10);
                } else {
                    Config.tableColor = new Color(7, 54, 13);
                }
                panel.setBackground(Config.tableColor);
                if (GameWindow.instance != null) {
                    GameWindow.instance.updateTheme();
                }
            }
        });

        JLabel resLabel = new JLabel("Window Resolution:");
        resLabel.setForeground(Color.WHITE);
        resLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] resOptions = {"800 x 600", "1024 x 768", "1280 x 720"};
        final JComboBox resBox = new JComboBox(resOptions);
        resBox.setMaximumSize(new Dimension(200, 40));
        resBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (Config.windowWidth == 1024) resBox.setSelectedIndex(1);
        else if (Config.windowWidth == 1280) resBox.setSelectedIndex(2);

        resBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.buttonOne(); 
                String selected = (String) resBox.getSelectedItem();
                if (selected.contains("1024")) {
                    Config.windowWidth = 1024; Config.windowHeight = 768;
                } else if (selected.contains("1280")) {
                    Config.windowWidth = 1280; Config.windowHeight = 720;
                } else {
                    Config.windowWidth = 800; Config.windowHeight = 600;
                }

                if (GameWindow.instance != null) {
                    GameWindow.instance.applyResolution(Config.windowWidth, Config.windowHeight);
                } else {
                    System.out.println("Resolution preset to " + Config.windowWidth + "x" + Config.windowHeight);
                }
            }
        });

        JButton closeBtn = new JButton("Back");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.buttonOne(); 
                new BlackjackStartWindow(); 
                frame.dispose();
            }
        });

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(musicBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(colorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(colorBox);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(resLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(resBox);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(closeBtn);
        panel.add(Box.createVerticalGlue());

        frame.add(panel);
        frame.setVisible(true);
    }
}