package gui;
import javax.swing.*;
import backend.BlackjackServer;
import backend.BlackjackClient; 

import java.awt.*;

public class HostServerWindow {
	private JFrame frame;
	
	public HostServerWindow() {
		
		frame = new JFrame("Host Server"); 
		frame.setSize(800,600); 
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel(); 
		panel.setBackground(new Color(20,50,30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
		
		JLabel title = new JLabel("HOST MULTIPLAYER");
		title.setFont(new Font("Times New Roman", Font.BOLD, 24));
		title.setForeground(Color.PINK);
		title.setAlignmentX(Component.CENTER_ALIGNMENT); 
		
		JLabel info = new JLabel("Server will run on port 8888"); 
		info.setForeground(Color.LIGHT_GRAY);
		info.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JButton startButton = new JButton("Start Server"); 
		JButton returnButton = new JButton("Return");
		
		startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		startButton.addActionListener(e -> {
			SoundManager.buttonOne();
			System.out.println("Server started...");
			
			new Thread(() -> {
				try {
					BlackjackServer.main(null); 
				} catch(Exception ex) {
					ex.printStackTrace(); 
				}
			}).start(); 
			
			JOptionPane.showMessageDialog(frame,  "Server started on port 8888!");
			
			BlackjackClient client = new BlackjackClient("localhost"); 
			
			new OnlineGameConnector(client); 
			
			frame.dispose();
			
		});
		
		returnButton.addActionListener(e -> {
			SoundManager.buttonOne();
			frame.dispose();
			new BlackjackStartWindow(); 
		});
		
		panel.add(Box.createVerticalGlue());
		panel.add(title);
		panel.add(Box.createRigidArea(new Dimension(0,20)));
		panel.add(info); 
		panel.add(Box.createRigidArea(new Dimension(0,30)));
		panel.add(startButton);
		panel.add(Box.createRigidArea(new Dimension(0,15)));
		panel.add(returnButton); 
		panel.add(Box.createVerticalGlue()); 
		
		frame.add(panel); 
		frame.setVisible(true);
		}

}
