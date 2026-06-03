package gui;
import javax.swing.*; 
import java.awt.*; 

public class RulesWindow {
    public RulesWindow() {
        JFrame frame = new JFrame("Blackjack Rules"); 
        frame.setSize(800, 600); 
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null); 
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String rulesText = "=== BLACKJACK RULES ===\n\n" +
            "1. Objective: Beat the dealer's hand without going over 21.\n\n" +
            "2. Card Values:\n" +
            "   - Face cards (J, Q, K) are 10 points.\n" +
            "   - Aces are 1 or 11 points (whichever helps more).\n" +
            "   - Other cards are their face value.\n\n" +
            "3. Hitting: Ask for another card to increase your total.\n\n" +
            "4. Standing: Keep your current hand and end your turn.\n\n" +
            "5. Busting: If your total exceeds 21, you lose immediately.\n\n" +
            "6. 5-Card Charlie (Special Rule):\n" +
            "   - If you hold 5 cards without busting, you win automatically!\n\n" +
            "7. Dealer Logic: The dealer must hit until they reach at least 17.\n";

        JTextArea textArea = new JTextArea(rulesText);
        textArea.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        textArea.setEditable(false); 
        textArea.setLineWrap(true);  
        textArea.setWrapStyleWord(true);
        textArea.setBackground(frame.getBackground()); 

        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("I UNDERSTAND");
        closeButton.addActionListener(e -> {
        	SoundManager.buttonOne(); 
        	new BlackjackStartWindow(); 
        	frame.dispose(); 
        });
        
        panel.add(closeButton, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }
}