import javax.swing.*;
import java.awt.*; 

public class PlayerWindow {
	public PlayerWindow() {
		JFrame frame = new JFrame("BLACKJACK+"); 
		frame.setSize(800,600); 	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setLocationRelativeTo(null); 
		
		//button
		JButton CvP = new JButton("CvP"); 
		JButton PvP = new JButton("PvP"); 
		JButton returnButton = new JButton("Return"); 
		
		//label
		JLabel title = new JLabel("Choose Mode");
		title.setFont(new Font("Times New Roman", Font.BOLD, 50));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		title.setForeground(Color.PINK);
		
		ImageIcon bgImage = new ImageIcon("/Users/lucan/Downloads/blackjackbackground.png");
		
		
		//panel
		JLabel panel = new JLabel(bgImage); 
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		CvP.setAlignmentX(Component.CENTER_ALIGNMENT);
		PvP.setAlignmentX(Component.CENTER_ALIGNMENT);
		returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		Dimension buttonSize = new Dimension(200,50); 
		
		CvP.setMaximumSize(buttonSize);
		PvP.setMaximumSize(buttonSize);
		returnButton.setMaximumSize(buttonSize);
		
		CvP.addActionListener(e -> {
			System.out.println("Computer vs Player chosen"); 
			new GameWindow("COMPUTER", 3, 42);
			frame.dispose(); 
		});
		
		PvP.addActionListener(e -> {
			System.out.println("Player vs Player chosen");
			new GameWindow("PLAYER", 3, 42); 
			frame.dispose(); 
		});		
		
		returnButton.addActionListener( e-> {
			System.out.println("Return to main menu"); 

		});
		
		panel.add(Box.createVerticalGlue());
		panel.add(title); 
		panel.add(Box.createRigidArea(new Dimension(0,40)));
		panel.add(CvP); 
		panel.add(Box.createRigidArea(new Dimension(0,20))); 
		panel.add(PvP); 
		panel.add(Box.createRigidArea(new Dimension(0, 20))); 
		panel.add(returnButton);
		panel.add(Box.createRigidArea(new Dimension(0,20)));
		panel.add(Box.createVerticalGlue()); 
		
		frame.add(panel); 
		
		frame.setVisible(true); 
	}
}