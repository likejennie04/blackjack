package gui;
import javax.swing.*;

import java.awt.*; 

public class OnlineModeWindow {
	public OnlineModeWindow() {
		JFrame frame = new JFrame("BLACKJACK+"); 
		frame.setSize(800,600); 	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setLocationRelativeTo(null); 
		
		//button
		JButton hostGame = new JButton("Host Game"); 
		JButton joinGame = new JButton("Join Game"); 
		JButton returnButton = new JButton("Return"); 
		
		//label
		JLabel title = new JLabel("Choose Mode");
		title.setFont(new Font("Times New Roman", Font.BOLD, 50));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		title.setForeground(Color.PINK);
		
        ImageIcon bgImage = new ImageIcon(getClass().getResource("/image/blackjackbackground.png")); 
		
		
		//panel
		JLabel panel = new JLabel(bgImage); 
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		hostGame.setAlignmentX(Component.CENTER_ALIGNMENT);
		joinGame.setAlignmentX(Component.CENTER_ALIGNMENT);
		returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		Dimension buttonSize = new Dimension(200,50); 
		
		hostGame.setMaximumSize(buttonSize);
		joinGame.setMaximumSize(buttonSize);
		returnButton.setMaximumSize(buttonSize);
		
		hostGame.addActionListener(e -> {
			SoundManager.buttonThree(); 
			System.out.println("Hosting server"); 
			new HostServerWindow(); 
			frame.dispose(); 
		});
		
		joinGame.addActionListener(e -> {
			SoundManager.buttonThree(); 
			System.out.println("Joining server");
			new JoinServerWindow(); 
			frame.dispose(); 
		});		
		
		returnButton.addActionListener( e-> {
			SoundManager.buttonOne(); 
			System.out.println("Return to main menu"); 
			new BlackjackStartWindow(); 
			frame.dispose();

		});
		
		panel.add(Box.createVerticalGlue());
		panel.add(title); 
		panel.add(Box.createRigidArea(new Dimension(0,40)));
		panel.add(hostGame); 
		panel.add(Box.createRigidArea(new Dimension(0,20))); 
		panel.add(joinGame); 
		panel.add(Box.createRigidArea(new Dimension(0, 20))); 
		panel.add(returnButton);
		panel.add(Box.createRigidArea(new Dimension(0,20)));
		panel.add(Box.createVerticalGlue()); 
		
		frame.add(panel); 
		
		frame.setVisible(true); 
	}
}