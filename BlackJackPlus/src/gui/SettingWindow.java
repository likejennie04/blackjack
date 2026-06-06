package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; 

public class SettingWindow {
    private JSpinner playerSpinner; 
    
    public SettingWindow() {
       
        final JFrame frame = new JFrame("Game Settings");
        frame.setSize(400, 550); 
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
        musicBtn.setMaximumSize(new Dimension(200, 40));
        musicBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.buttonOne(); 
                SoundManager.toggleMusic(); 
                
                if (SoundManager.isMusicPlaying()) {
                    musicBtn.setText("Music ON");
                } else {
                    musicBtn.setText("Music OFF");
                }
            }
        });

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
                SoundManager.buttonOne(); 
                
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
                
                updateActiveOnlineConnectorTheme();
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
                }
                // 🎯 融入联机：让分辨率修改也能无缝自适应拉伸联机大盘！
                updateActiveOnlineConnectorResolution();
            }
        });
        
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        row.setOpaque(false); 
        
        JLabel numLabel = new JLabel("Number of Players (1-4): "); 
        numLabel.setForeground(Color.WHITE);
        
        SpinnerModel model = new SpinnerNumberModel(Config.participantCount, 1, 4, 1); 
        playerSpinner = new JSpinner(model); 
        playerSpinner.setPreferredSize(new Dimension(60, 25));
        
        row.add(numLabel); 
        row.add(playerSpinner);
        row.setMaximumSize(new Dimension(300, 40));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // --- Action Buttons ---
        JButton saveButton = new JButton("Save Changes"); 
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setMaximumSize(new Dimension(200, 40));
        saveButton.addActionListener(e -> {
            SoundManager.buttonOne(); 
            
            Config.participantCount = (int) playerSpinner.getValue(); 
            System.out.println("Config updated. Total participants: " + Config.participantCount);
            
            if (GameWindow.instance != null) {
                GameWindow.instance.updateTheme();
            }
            updateActiveOnlineConnectorTheme();
            frame.dispose();
        });

        JButton closeBtn = new JButton("Return");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setMaximumSize(new Dimension(200, 40));
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.buttonOne();  
                frame.dispose();
            }
        });

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        panel.add(musicBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        panel.add(colorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(colorBox);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        panel.add(resLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(resBox);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(row); 
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(saveButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(closeBtn);
        panel.add(Box.createVerticalGlue());

        frame.add(panel);
        frame.setVisible(true);
    }

   
    private void updateActiveOnlineConnectorTheme() {
        for (Window w : Window.getWindows()) {
            if (w instanceof JFrame && w.isVisible()) {
                JFrame jf = (JFrame) w;
                if (jf.getTitle().contains("Online Mode") || jf.getTitle().contains("Arena")) {
                    jf.getContentPane().setBackground(Config.tableColor);
                    // 动态查找组件内的 tablePanel 层重新变色
                    SwingUtilities.invokeLater(() -> {
                        for(Component comp : jf.getContentPane().getComponents()) {
                            if(comp instanceof JPanel) {
                                comp.setBackground(Config.tableColor);
                                for(Component subComp : ((JPanel)comp).getComponents()) {
                                    if(subComp instanceof JScrollPane) {
                                        ((JScrollPane)subComp).getViewport().getView().setBackground(Config.tableColor);
                                    }
                                }
                            }
                        }
                        w.validate();
                        w.repaint();
                    });
                }
            }
        }
    }

    
    private void updateActiveOnlineConnectorResolution() {
        for (Window w : Window.getWindows()) {
            if (w instanceof JFrame && w.isVisible()) {
                JFrame jf = (JFrame) w;
                if (jf.getTitle().contains("Online Mode") || jf.getTitle().contains("Arena")) {
                    w.setSize(Config.windowWidth, Config.windowHeight);
                    w.setLocationRelativeTo(null);
                }
            }
        }
    }
}