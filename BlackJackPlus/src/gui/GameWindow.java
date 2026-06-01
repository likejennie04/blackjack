package gui;

import javax.swing.*;
import backend.Computer;
import backend.Deck;
import backend.Hand;
import backend.House;
import backend.Player;
import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList;
import java.util.Random;

public class GameWindow {
    
    
    public static GameWindow instance; 

    private JFrame frame;
    private JPanel mainPanel; 
    private JPanel tablePanel; 
    private JLabel statusLabel; 
    private JButton hitButton; 
    private JButton standButton; 
    private JButton settingButton;

    private String gameMode;
    private Deck cardDeck; 
    private House dealer; 
    private ArrayList<Computer> aiList;
    private ArrayList<Player> playerList; 
    private int currentPlayerIndex = 0; 
    private ArrayList<Hand> playOrder = new ArrayList<>(); 

    public GameWindow(String mode, int participants, int seed) {
       instance = this;
        this.gameMode = mode;
        
        /* 1. Initialize backend logic structures */
        this.cardDeck = new Deck();
        this.cardDeck.shuffle(seed);
        this.playerList = new ArrayList<>(); 
        this.dealer = new House();
        this.aiList = new ArrayList<>();
        
        if(gameMode.equals("PLAYER")) {
            for (int i= 0; i < participants; i++) playerList.add(new Player()); 
        } else if (this.gameMode.equals("COMPUTER")) {
            playerList.add(new Player()); 
            Random aiRand = new Random(seed); 
            for (int i =0; i< participants -1; i ++) {
                aiList.add(new Computer(aiRand, cardDeck, i+ 2)); 
            }
        }
        
        for (Player p: playerList) playOrder.add(p);
        for (Computer ai : aiList) playOrder.add(ai);
        playOrder.add(dealer); 
        
        for (int round =0; round < 2; round ++) {
            for (Hand h: playOrder) h.addCard(cardDeck.dealCard());
        }
        
        /* 2. Build UI Layout Components */
        frame = new JFrame("BLACKJACK+"); // FIXED: Added missing initialization
        this.applyResolution(Config.windowWidth, Config.windowHeight);
        frame.setSize(Config.windowWidth, Config.windowHeight); 
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Config.tableColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel headerPanel = new JPanel(new BorderLayout()); 
        headerPanel.setOpaque(false);
        statusLabel = new JLabel("Your Turn! Use the control panel buttons below.", JLabel.CENTER); 
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(new Color(220, 220, 100)); 
        headerPanel.add(statusLabel, BorderLayout.CENTER); 
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        tablePanel = new JPanel(); 
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS)); 
        tablePanel.setBackground(Config.tableColor);
        
        JScrollPane tableScroll = new JScrollPane(tablePanel);
        tableScroll.setBorder(null);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);
        
        hitButton = new JButton("HIT");
        standButton = new JButton("STAND");
        settingButton = new JButton("SETTING");
        
        styleActionButton(hitButton, new Color(180, 40, 40));
        styleActionButton(standButton, new Color(40, 100, 180));
        styleActionButton(settingButton, new Color(100, 100, 100));
        
        /* Action Listeners using Anonymous Inner Classes (Student Style) */
        settingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Debug: Opening settings from game window");
                new SettingWindow(); 
            }
        });

        hitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handelHitAction();
            }
        });

        standButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleStandAction();
            }
        });
        
        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        controlPanel.add(settingButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        buildTableRows(); 
        frame.setVisible(true);
    }

    /* --- UI Refresh & Scaling Methods --- */

    /**
     * Resizes the frame and refreshes the content layout.
     * Called by SettingWindow.
     */
    public void applyResolution(int w, int h) {
    if (frame != null) {
       
        frame.setSize(w, h);
        frame.getContentPane().setPreferredSize(new Dimension(w, h));
        
        
        frame.pack(); 
        
        
        frame.setLocationRelativeTo(null); 
        
        frame.getContentPane().revalidate();
        frame.repaint();
        
        
        JOptionPane.showMessageDialog(frame, "Resolution matched to " + w + "x" + h);
        System.out.println("Debug: Applied " + w + "x" + h);
    }
}

    public JFrame getFrame() {
        return this.frame;
    }

    /**
     * Updates the background color of panels in real-time.
     */
    public void updateTheme() {
        if (mainPanel != null) mainPanel.setBackground(Config.tableColor);
        if (tablePanel != null) tablePanel.setBackground(Config.tableColor);
        buildTableRows(); // Re-draw rows to apply new background
        frame.repaint();
    }

    private void buildTableRows() {
        tablePanel.removeAll(); 
        String[] dealerCards = dealer.getHandStrings();
        if (hitButton.isEnabled()) {
            dealerCards = (dealerCards.length >= 2) ? new String[]{"HIDDEN", dealerCards[1]} : new String[]{"HIDDEN"};
            tablePanel.add(createPlayerRowPanel("Dealer (House)", "??", dealerCards));
        } else {
            tablePanel.add(createPlayerRowPanel("Dealer (House)", String.valueOf(dealer.getScore()), dealerCards));
        }
        tablePanel.add(Box.createRigidArea(new Dimension(0, 10)));
    
        for (int i = 0; i < playerList.size(); i++) {
            Player p = playerList.get(i); 
            tablePanel.add(createPlayerRowPanel("Player " + (i+1), String.valueOf(p.getScore()), p.getHandStrings()));
            tablePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    
        if (gameMode.equals("COMPUTER")) {
            for (int i = 0; i < aiList.size(); i++) {
                Computer ai = aiList.get(i);
                tablePanel.add(createPlayerRowPanel("AI Bot " + (i + 2), String.valueOf(ai.getScore()), ai.getHandStrings()));
                tablePanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private JPanel createPlayerRowPanel(String name, String currentScore, String[] cards) {
        JPanel row = new JPanel(new BorderLayout()); 
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setBackground(Config.tableColor); 
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JPanel identityLabelPanel = new JPanel(new GridLayout(2, 1)); 
        identityLabelPanel.setOpaque(false); 
        identityLabelPanel.setPreferredSize(new Dimension(150, 0)); 
        
        JLabel nameTxT = new JLabel(name); 
        nameTxT.setFont(new Font("Arial", Font.BOLD, 15)); 
        nameTxT.setForeground(Color.WHITE);
        
        JLabel scoreTxT = new JLabel("Score: " + currentScore); 
        scoreTxT.setFont(new Font("Arial", Font.PLAIN, 13));
        scoreTxT.setForeground(new Color(220, 220, 220));
        
        identityLabelPanel.add(nameTxT); 
        identityLabelPanel.add(scoreTxT); 
        row.add(identityLabelPanel, BorderLayout.WEST); 
        
        JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        handPanel.setOpaque(false);
        
        for (String card : cards) {
            JPanel cardVisual = new JPanel(new BorderLayout()); 
            cardVisual.setPreferredSize(new Dimension(50, 65)); 
            cardVisual.setBackground(Color.WHITE); 
            cardVisual.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); 
            JLabel cardLabel = new JLabel(card, JLabel.CENTER); 
            cardLabel.setFont(new Font("Arial", Font.BOLD, 14));
            if (card.contains("h") || card.contains("d") || card.contains("♥") || card.contains("♦")) {
                cardLabel.setForeground(Color.RED);
            } else {
                cardLabel.setForeground(Color.BLACK); 
            }
            cardVisual.add(cardLabel, BorderLayout.CENTER);
            handPanel.add(cardVisual); 
        }
        row.add(handPanel, BorderLayout.CENTER); 
        return row; 
    }

    /* --- Game Logic Methods --- */
    
    private void handelHitAction() {
        Player currentPlayer = playerList.get(currentPlayerIndex);
        currentPlayer.addCard(cardDeck.dealCard());
        if (currentPlayer.getScore() > 21) {
            statusLabel.setText("Player " + (currentPlayerIndex + 1) + " busted!");
            handleStandAction();
        } else {
            statusLabel.setText("Player " + (currentPlayerIndex + 1) + " drew a card.");
        }
        buildTableRows();
    }

    private void handleStandAction() {
        System.out.println("Debug: Player stands, switching turns...");
        if (gameMode.equals("COMPUTER")) {
            Thread[] aiThreads = new Thread[aiList.size()];
            for (int i = 0; i < aiList.size(); i++) {
                aiThreads[i] = new Thread(aiList.get(i)); 
                aiThreads[i].start();
            }
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (Thread t : aiThreads) t.join();
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false); 
                    dealer.runTurn(cardDeck); 
                    return null;
                }
                @Override
                protected void done() {
                    buildTableRows();
                    evaluateFinalWinners();
                }
            }.execute();
        } else {
            currentPlayerIndex++; 
            if (currentPlayerIndex < playerList.size()) {
                statusLabel.setText("Pass device to Player " + (currentPlayerIndex + 1));
                buildTableRows(); 
                return; 
            }
            hitButton.setEnabled(false); 
            standButton.setEnabled(false);
            dealer.runTurn(cardDeck);
            buildTableRows(); 
            evaluateFinalWinners(); 
        }
    }

    private void evaluateFinalWinners() {
    int dealerScore = dealer.getScore();
    StringBuilder results = new StringBuilder(); 
    StringBuilder dialogMessage = new StringBuilder();
    
    dialogMessage.append("🏆 === GAME OVER SUMMARY === 🏆\n\n");
    dialogMessage.append(String.format("🏠 Dealer (House) Score: %d %s\n", 
        dealerScore, (dealerScore > 21 ? "[BUSTED! 💥]" : "")));
    dialogMessage.append("--------------------------------------------------\n");

    for (int i = 0; i < playerList.size(); i++) {
        Player p = playerList.get(i); 
        int score = p.getScore(); 
        String outcome = "";

        results.append("P").append(i+1).append(": "); 
        if (score > 21) {
            results.append("Bust");
            outcome = "❌ BUSTED (Lose)";
        } else if (p.getHandStrings().length == 5 && score <= 21) {
            results.append("5-Card Win");
            outcome = "👑 WIN (5-Card Charlie!)";
        } else if (dealerScore > 21) {
            results.append("Win");
            outcome = "🎉 WIN (Dealer Busted)";
        } else if (score > dealerScore) {
            results.append("Win");
            outcome = "🎉 WIN (Higher Score)";
        } else if (score < dealerScore) {
            results.append("Lose");
            outcome = "😭 LOSE";
        } else {
            results.append("Draw");
            outcome = "🤝 DRAW (Push)";
        } 
        
        if (i < playerList.size() - 1) results.append(" | ");
        dialogMessage.append(String.format("👤 Player %d:  Score: %d  ->  %s\n", (i + 1), score, outcome));
    }

    if (gameMode.equals("COMPUTER")) {
        dialogMessage.append("--------------------------------------------------\n");
        for (int i = 0; i < aiList.size(); i++) {
            Computer ai = aiList.get(i);
            int score = ai.getScore();
            String outcome = "";

            if (score > 21) outcome = "❌ BUSTED";
            else if (ai.getHandStrings().length == 5 && score <= 21) outcome = "👑 WIN (5-Card Charlie!)";
            else if (dealerScore > 21 || score > dealerScore) outcome = "🎉 WIN";
            else if (score < dealerScore) outcome = "😭 LOSE";
            else outcome = "🤝 DRAW";

            dialogMessage.append(String.format("🤖 AI Bot %d:  Score: %d  ->  %s\n", (i + 2), score, outcome));
        }
    }

    statusLabel.setText(results.toString());

    JOptionPane.showMessageDialog(
        frame, 
        dialogMessage.toString(), 
        "Game Results", 
        JOptionPane.INFORMATION_MESSAGE
    );
}

    private void styleActionButton(JButton button, Color bg) {
        button.setPreferredSize(new Dimension(120, 40)); 
        button.setBackground(bg); 
        button.setForeground(Color.WHITE); 
        button.setFont(new Font("Arial", Font.BOLD, 14)); 
        button.setFocusPainted(false); 
        button.setBorderPainted(false); 
        button.setOpaque(true); 
    }
}