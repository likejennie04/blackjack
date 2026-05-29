package gui;
import javax.swing.*;
import backend.BlackjackClient;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;

public class OnlineGameConnector {
	private JFrame frame; 
	private BlackjackClient client; 
	
	//UI elements
	private JLabel statusLabel;
	private JLabel playerScoreLabel; 
	private JPanel playerCardPanel; 
	private JPanel dealerCardPanel;
	private JButton hitButton; 
	private JButton standButton; 
	
	public OnlineGameConnector(BlackjackClient client) {
		this.client = client; 
		initializeGUI(); 
		startServerListener(); 
	}
	
	private void initializeGUI() {
		frame = new JFrame("Blackjack Multiplayer Arena"); 
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(new Color(20,50,30));
		mainPanel.setLayout(new BorderLayout());
		
		//top status area
		statusLabel = new JLabel("Waiting for game to start...", JLabel.CENTER);
		statusLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
		statusLabel.setForeground(Color.WHITE);
		mainPanel.add(statusLabel, BorderLayout.NORTH); 
		
		//center area
		JPanel tablePanel = new JPanel(new GridLayout(2,1)); 
		tablePanel.setOpaque(false);
		
		dealerCardPanel = new JPanel(); 
		dealerCardPanel.setBorder(BorderFactory.createTitledBorder("Dealer's Hand"));
		playerCardPanel = new JPanel(); 
		playerCardPanel.setBorder(BorderFactory.createTitledBorder("Your Hand"));
		
		tablePanel.add(dealerCardPanel); 
		tablePanel.add(playerCardPanel); 
		mainPanel.add(tablePanel, BorderLayout.CENTER);
		
		//Bottom control area
		JPanel controlPanel = new JPanel(); 
		controlPanel.setOpaque(false); 
		
		hitButton = new JButton("Hit"); 
		standButton = new JButton("Stand"); 
		
		hitButton.setEnabled(false);
		standButton.setEnabled(false);
		
		//send command
		hitButton.addActionListener( e -> client.sendMove("hit"));
		standButton.addActionListener(e -> client.sendMove("stand"));
		
		controlPanel.add(hitButton);
		controlPanel.add(standButton);
		mainPanel.add(controlPanel, BorderLayout.SOUTH);
		
		frame.add(mainPanel);
		frame.setVisible(true);
		
	}
	
	private void startServerListener() {
		new Thread(()-> {
			try {
				BufferedReader in = client.getInputStream(); 
				String serverMessage;
				
				while((serverMessage = in.readLine()) != null) {
					final String msg = serverMessage; 
					
					SwingUtilities.invokeLater(() -> {
						processServerMessage(msg); 
					});
				}
			} catch (IOException e) {
				SwingUtilities.invokeLater(() -> {
					statusLabel.setText("Connection to server lost."); 
					hitButton.setEnabled(false);
					standButton.setEnabled(false);
				});
			}
		}).start();
	}
	
	private void processServerMessage(String message) {
		System.out.println("[Server Data]: " + message); 
		
		if(message.contains("Yout Turn")) {
			statusLabel.setText("It's your turn!");
			hitButton.setEnabled(true);
			standButton.setEnabled(true);
		} else if (message.contains("Waiting")) {
			statusLabel.setText(("Waiting for other Player..."));
			hitButton.setEnabled(false); 
			standButton.setEnabled(false); 
		} else if (message.contains("Bust") || message.contains("Wins") || message.contains("Dealer")) {
			statusLabel.setText(message);
		}
	}
 
}
