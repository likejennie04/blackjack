package gui;

import javax.swing.*;
import backend.OnlineGameState;
import backend.BlackjackClient;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import backend.PlayerSnapshot;
import backend.RosterPlayer; 

public class OnlineGameConnector {
    private JFrame frame;
    private BlackjackClient client;
    private OnlineGameState gameState;

    // --- Core Containers ---
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JPanel tablePanel; 
    private JPanel rosterPanel; 
    private JButton hitButton;
    private JButton standButton;
    private JButton settingButton; 
    private JButton returnButton;
    private Timer summaryTimer; 
    private JFrame resultWindow; 

    
    public OnlineGameConnector(BlackjackClient client) {
        this.client = client;
        gameState = new OnlineGameState(); 
        
        try {
            if (AvatarManager.getAllAvatars().isEmpty()) {
                Class.forName("gui.AvatarManager");
            }
        } catch (Exception ignored) {}

        initializeGUI();
        startServerListener();
        
        client.sendMove("REQUEST_SNAPSHOT");
    }

    private void initializeGUI() {
        frame = new JFrame("Blackjack+ Arena (Online Mode)");
        frame.setSize(Config.windowWidth > 0 ? Config.windowWidth : 900, 
                      Config.windowHeight > 0 ? Config.windowHeight : 700); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Config.tableColor != null ? Config.tableColor : new Color(20, 50, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        statusLabel = new JLabel("Game Started! Good Luck!", JLabel.CENTER);
        statusLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        statusLabel.setForeground(new Color(220, 220, 100));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        rosterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        rosterPanel.setOpaque(false);
        rosterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 50)));

        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(rosterPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Config.tableColor != null ? Config.tableColor : new Color(20, 50, 30));
        
        JScrollPane tableScroll = new JScrollPane(tablePanel);
        tableScroll.setBorder(null);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);

        hitButton = new JButton("HIT");
        standButton = new JButton("STAND");
        settingButton = new JButton("SETTINGS");
        returnButton = new JButton("RETURN");
        
        styleActionButton(hitButton, new Color(180, 40, 40));
        styleActionButton(standButton, new Color(40, 100, 180));
        styleActionButton(settingButton, new Color(100, 100, 100));
        styleActionButton(returnButton, new Color(100, 100, 100));        

        setPlayerControls(false); 
        
        hitButton.addActionListener(e -> {
            SoundManager.hitButton();
            client.sendMove("hit");
        });
        standButton.addActionListener(e -> {
            SoundManager.standButton();
            client.sendMove("stand");
        });
        
        settingButton.addActionListener(e -> {
            SoundManager.buttonOne();
            new SettingWindow(); 
        });

        returnButton.addActionListener(e -> {
            SoundManager.buttonOne();
            executeQuitSequence();
        });

        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        controlPanel.add(settingButton);
        controlPanel.add(returnButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void styleActionButton(JButton button, Color bg) {
        button.setPreferredSize(new Dimension(120, 40)); 
        button.setBackground(bg); 
        button.setForeground(Color.WHITE); 
        button.setFont(new Font("Times New Roman", Font.BOLD, 14)); 
        button.setFocusPainted(false); 
        button.setBorderPainted(false); 
        button.setOpaque(true); 
    }

    public void updateTheme() {
        if (mainPanel != null && tablePanel != null) {
            mainPanel.setBackground(Config.tableColor);
            tablePanel.setBackground(Config.tableColor);
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    public void applyResolution(int width, int height) {
        if (frame != null) {
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
        }
    }

    
    private void startServerListener() {
        new Thread(() -> {
            try {
                BufferedReader in = client.getInputStream();
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    final String msg = serverMessage;
                    SwingUtilities.invokeLater(() -> processServerMessage(msg));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Connection lost.");
                    setPlayerControls(false); 
                });
            }
        }).start();
    }
    
    
    private void refreshTable() {

        tablePanel.removeAll();

        for (PlayerSnapshot player : gameState.getPlayerSnapshots()) {

            tablePanel.add(
                    PlayerRowRenderer.createPlayerRow(
                            player,
                            client)
            );

            tablePanel.add(
                    Box.createRigidArea(
                            new Dimension(0,10))
            );
        }

        tablePanel.revalidate();
        tablePanel.repaint();
    }
    
  

 
    
 

    private void updateVisualRoster() {

        rosterPanel.removeAll();

        rosterPanel.add(
                RosterRenderer.createDealerBlock(
                        gameState.getCurrentTurnPlayer())
        );

        for (RosterPlayer player : gameState.getRosterPlayers()) {

            rosterPanel.add(
                    RosterRenderer.createPlayerBlock(
                            player,
                            gameState.getCurrentTurnPlayer(),
                            client)
            );
        }

        rosterPanel.revalidate();
        rosterPanel.repaint();
  

    }
    public void setResultWindow(JFrame window) {
    	this.resultWindow = window; 
    }
    
    private void setPlayerControls(boolean enabled) {
    	hitButton.setEnabled(enabled);
    	standButton.setEnabled(enabled);
    }

    private void executeQuitSequence() {
        client.disconnect();
        frame.dispose();
        if (resultWindow != null) {
            resultWindow.dispose();
        }
        new BlackjackStartWindow();
    }
    
    private void handleRosterUpdate(String message) {

        String data =
                message.substring(
                        message.indexOf("ROSTER_UPDATE:") + 14)
                        .trim();

        gameState.updateRoster(data);

        refreshRoster();
    }
    
    private void refreshRoster() {
        updateVisualRoster();
    }
    
    private void handleCurrentTurn(String message) {
    	 int index = message.indexOf("CURRENT_TURN:");
         gameState.setCurrentTurnPlayer(message.substring(index + 13).trim());  
         
        refreshRoster(); 
    }
    
    private void handleTableSnapshot(String message) {
    	System.out.println("Snapshot recieved"); 
    	String snapshotData = message.substring(message.indexOf("TABLE_SNAPSHOT:") + 15).trim();
    	gameState.updateSnapshot(snapshotData);
    	refreshTable();
    }
    
    private void clearTable() {

        tablePanel.removeAll();
        tablePanel.revalidate();
        tablePanel.repaint();
    }
    
    
    private void handleNewRound() {
    	System.out.println("New Round recieved"); 

    	if (summaryTimer != null)  {
    		summaryTimer.stop(); 
    		summaryTimer = null; 
    	}
    	
        statusLabel.setText("New Round Started! Good Luck!");
        
        if (resultWindow != null) {
            resultWindow.dispose();
            resultWindow = null;
        }
       clearTable(); 
    }

    private void handleGameOver() {

        statusLabel.setText("GAME OVER...");

        hitButton.setEnabled(false);
        standButton.setEnabled(false);

        String cachedSnapshot =
                gameState.getLatestSnapshotRaw();

        if (cachedSnapshot == null) {
            cachedSnapshot =
                    "Dealer=10s,8c;YourName=Kc,Ah";
        }

        String finalSnapshot = cachedSnapshot;

        if (summaryTimer != null) {
            summaryTimer.stop();
        }

        summaryTimer = new Timer(3000, e -> {
            FinalBoardPrint.showOnlineSummary(
                    frame,
                    finalSnapshot,
                    client,
                    this);
        });

        summaryTimer.setRepeats(false);
        summaryTimer.start();
    }
    
    
    
    

    private void processServerMessage(String message) {
        System.out.println("[Server Data]: " + message);

       
        if (message.contains("GAME_STARTED") || message.contains("ROUND_START") || message.contains("NEW_ROUND")) {
        	handleNewRound();
        }
        	
        if (message.contains("TABLE_SNAPSHOT:")) {
           handleTableSnapshot(message); 
        }
        

        if (message.contains("ROSTER_UPDATE:")) {
            handleRosterUpdate(message); 
        }
        
        if (message.contains("WAIT_FOR_HOST")) {
            statusLabel.setText("Waiting for host to restart the game...");
            setPlayerControls(false); 
        }
        if (message.contains("YOU_ARE_HOST")) {
            statusLabel.setText("You are the host.");
        }

        if (message.contains("UNLOCK_ACTIONS_FOR_CLIENT")) {
            statusLabel.setText("★ Your Turn! Make your move. ★");
            setPlayerControls(true); 
        } 
        else if (message.contains("LOCK_ACTIONS_FOR_CLIENT")) {
            statusLabel.setText("Waiting for other players...");
            setPlayerControls(false); 
        }
        
        if (message.contains("CURRENT_TURN:")) {
            handleCurrentTurn(message); 
        }
        if (message.contains("GAME_OVER_SUMMARY:")
                || message.contains("SHOW_FINAL_SUMMARY:")) {
        	handleGameOver(); 
        }
            
    }
}