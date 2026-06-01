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
        
        frame = new JFrame("BLACKJACK+"); 
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
        statusLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
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
        
        settingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	SoundManager.buttonOne(); 
                System.out.println("Debug: Opening settings from game window");
                new SettingWindow(); 
            }
        });

        hitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	SoundManager.hitButton();
                handelHitAction();
            }
        });

        standButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	SoundManager.standButton(); 
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

    public void applyResolution(int w, int h) {
        if (frame != null) {
            frame.setSize(w, h);
            frame.getContentPane().setPreferredSize(new Dimension(w, h));
            frame.pack(); 
            frame.setLocationRelativeTo(null); 
            frame.getContentPane().revalidate();
            frame.repaint();
        }
    }

    public JFrame getFrame() {
        return this.frame;
    }
    
    public void updateTheme() {
        if (mainPanel != null) mainPanel.setBackground(Config.tableColor);
        if (tablePanel != null) tablePanel.setBackground(Config.tableColor);
        buildTableRows(); 
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
        nameTxT.setFont(new Font("Times New Roman", Font.BOLD, 15)); 
        nameTxT.setForeground(Color.WHITE);
        
        JLabel scoreTxT = new JLabel("Score: " + currentScore); 
        scoreTxT.setFont(new Font("Times New Roman", Font.PLAIN, 13));
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
            cardLabel.setFont(new Font("Times New Roman", Font.BOLD, 14));
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
        // ◄--- FIXED: Added "this" as the final parameter so FinalBoardPrint has the instance reference
    	String bannerResultsText = FinalBoardPrint.showSummary(frame, dealer, playerList, aiList, gameMode, this); 
    	statusLabel.setText(bannerResultsText); 
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
    
    public void restartMatch() {
    	System.out.println("Reseting table for next round...."); 
    	this.cardDeck = new Deck(); 
    	this.cardDeck.shuffle(new Random().nextInt(10000));
    	
    	dealer.clearHand(); 
    	for (Player p : playerList) p.clearHand(); 
    	for (Computer ai : aiList) ai.clearHand(); 
    	
    	currentPlayerIndex = 0; 
    	
    	for (int round =0; round<2; round++ ) {
    		for (Player p : playerList) {
    			p.addCard(cardDeck.dealCard());
    		}
    		for (Computer ai : aiList) {
    			ai.addCard(cardDeck.dealCard());
    		}
    		
    		dealer.addCard(cardDeck.dealCard()); 
    	}
    	
    	hitButton.setEnabled(true);
    	standButton.setEnabled(true);
        statusLabel.setText("Your Turn! Use the control panel buttons below.");
    	buildTableRows(); 
    }
}