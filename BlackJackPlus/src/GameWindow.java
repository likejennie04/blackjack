import javax.swing.*; 
import java.awt.*; 
import java.util.ArrayList;
import java.util.Random;

public class GameWindow {
    private JFrame frame;
    private JPanel tablePanel; 
    private JLabel statusLabel; 
    private JButton hitButton; 
    private JButton standButton; 
    
    // --- CONNECTED BACKEND OBJECTS ---
    // Instead of an abstract engine, your actual backend classes live here!
    private String gameMode;
    private Deck cardDeck; 
    private House dealer; 
    private ArrayList<Computer> aiList;
    private ArrayList<Player> playerList; 
    private int currentPlayerIndex =0; 
    private ArrayList<Hand> playOrder = new ArrayList<>(); 

    public GameWindow(String mode, int participants, int seed) {
        this.gameMode = mode;
        
        // 1. Initialize your exact backend logic structures using screen inputs
        this.cardDeck = new Deck();
        this.cardDeck.shuffle(seed); // Calls your backend shuffle(int seed)
        
        this.playerList = new ArrayList<>(); 
        this.dealer = new House();
        this.aiList = new ArrayList<>();
        
        //player vs player mode 
        if(gameMode.equals("PLAYER")) {
        	for (int i= 0; i < participants; i++) {
        		Player p = new Player(); 
        	
        		playerList.add(p); 
        	}
        }
        
        //computer vs human mode 
        if (this.gameMode.equals("COMPUTER")) {
            //one human player
        	Player human = new Player(); 
 
        	
        	playerList.add(human); 
        	
        	Random aiRand = new Random(seed); 
        	
        	for (int i =0; i< participants -1; i ++) {
        		Computer ai= new Computer(aiRand, cardDeck, i+ 2); 
        	
        		aiList.add(ai); 
        	}
        	
        }
        
        //play order
        
        for (Player p: playerList) {
        	playOrder.add(p); 
        }
        
        for (Computer ai : aiList) {
        	playOrder.add(ai); 
        }
        
        playOrder.add(dealer); 
        
        for (int round =0; round < 2; round ++) {
        	for (Hand h: playOrder) {
        		h.addCard(cardDeck.dealCard());
        	}
        }
        
        // 3. Build UI Layout components
        frame = new JFrame("BLACKJACK+"); 
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); 
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 50, 30)); 
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
        tablePanel.setBackground(new Color(25, 60, 35));
        
        JScrollPane tableScroll = new JScrollPane(tablePanel);
        tableScroll.setBorder(null);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);
        
        hitButton = new JButton("HIT");
        standButton = new JButton("STAND");
        styleActionButton(hitButton, new Color(180, 40, 40));
        styleActionButton(standButton, new Color(40, 100, 180));
        
        hitButton.addActionListener(e -> handelHitAction());
        standButton.addActionListener(e -> handleStandAction());
        
        controlPanel.add(hitButton);
        controlPanel.add(standButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        
        // 4. Draw the newly dealt hands onto the visual interface panel canvas
        buildTableRows(); 
        frame.setVisible(true);
    }
        
    private void buildTableRows() {
        tablePanel.removeAll(); 
        
        // Pull dealer hand representations directly out of backend object array
        String[] dealerCards = dealer.getHandStrings();
        if (hitButton.isEnabled()) {
            if (dealerCards.length >= 2) {
                dealerCards = new String[]{"HIDDEN", dealerCards[1]};
            } else {
                dealerCards = new String[]{"HIDDEN"};
            }
            tablePanel.add(createPlayerRowPanel("Dealer (House)", "??", dealerCards));
        } else {
            tablePanel.add(createPlayerRowPanel("Dealer (House)", String.valueOf(dealer.getScore()), dealerCards));
        }
        tablePanel.add(Box.createRigidArea(new Dimension(0, 10)));
    
        for (int i =0; i <playerList.size(); i++) {
        	Player p = playerList.get(i); 
        	
        	tablePanel.add(
        			createPlayerRowPanel(
        					"Player " + (i+1),
        					String.valueOf(p.getScore()),
        					p.getHandStrings()
        					)
        			);
        	tablePanel.add(
        			Box.createRigidArea(new Dimension(0,10))
        	);
        }
        
        // Pull human data directly out of backend player class
        /*tablePanel.add(createPlayerRowPanel("Player 1 (You)", String.valueOf(humanUser.getScore()), humanUser.getHandStrings()));
        tablePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        *
        */
    
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
	
    private void handelHitAction() {

        Player currentPlayer =
                playerList.get(currentPlayerIndex);

        currentPlayer.addCard(cardDeck.dealCard());

        if (currentPlayer.getScore() > 21) {

            statusLabel.setText(
                    "Player "
                    + (currentPlayerIndex + 1)
                    + " busted!"
            );

            handleStandAction();

        } else {

            statusLabel.setText(
                    "Player "
                    + (currentPlayerIndex + 1)
                    + " drew a card."
            );
        }

        buildTableRows();
    }
       
	
    private void handleStandAction() {
        System.out.println("User chose to STAND");
        statusLabel.setText("Processing opponent turns...");
       
        
        if (gameMode.equals("COMPUTER")) {
            Thread[] aiThreads = new Thread[aiList.size()];
            for (int i = 0; i < aiList.size(); i++) {
                // Starts your backend multithreaded AI logic run method
                aiThreads[i] = new Thread(aiList.get(i)); 
                aiThreads[i].start();
            }
            
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (Thread t : aiThreads) {
                        t.join();
                    }
                    // Executes backend dealer rule algorithms
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
            	
            	statusLabel.setText(
            		"Pass device to Player" + (currentPlayerIndex + 1)
            	);
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
        
        for (int i = 0; i < playerList.size(); i++) {
        	Player p  = playerList.get(i); 
        	int score = p.getScore(); 
        	
        	results.append("Player ")
        			.append(i+1)
        			.append(": "); 
        	if (score > 21) {
        		results.append("Busts"); 
        	} else if (dealerScore > 21 || score > dealerScore) {
        		results.append("Win"); 
        	} else if (score<dealerScore) {
        		results.append("Lose"); 
        	} else {
        		results.append("Draw"); 
        	} 
        	
        	if (i < playerList.size() -1) {
        		results.append(" | "); 
        	}
        }
        	
        statusLabel.setText(results.toString());
        }

    //FORMATTING
    private JPanel createPlayerRowPanel(String name, String currentScore, String[] cards) {
        JPanel row = new JPanel(new BorderLayout()); 
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setBackground(new Color(15, 45, 25));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 80, 50), 1),
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
        scoreTxT.setForeground(new Color(180, 220, 190));
		
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
			
            // Formats console card tags (like "Ah", "10s") into pretty colors
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