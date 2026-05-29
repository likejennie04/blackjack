package gui;
import javax.swing.*;
import backend.BlackjackServer;
import backend.BlackjackClient; 
import java.awt.*;

public class JoinServerWindow {
	private JFrame frame;
	private JTextField ipField; 
	
	public JoinServerWindow() {
		frame = new JFrame("Join Server"); 
		frame.setSize(800,600); 
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel(); 
		panel.setBackground(new Color(20,50,30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
		
		JLabel title = new JLabel("JOIN MULTIPLAYER");
		title.setFont(new Font("Times New Roman", Font.BOLD, 24));
		title.setForeground(Color.PINK);
		title.setAlignmentX(Component.CENTER_ALIGNMENT); 
		
		JLabel promptLabel = new JLabel("Enter Host IP Address: "); 
		promptLabel.setForeground(Color.LIGHT_GRAY);
		promptLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		ipField = new JTextField("localhost", 15); 
		ipField.setMaximumSize(new Dimension(200,30));
		ipField.setHorizontalAlignment(JTextField.CENTER);
		ipField.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JButton joinButton = new JButton("Join Game"); 
		JButton returnButton = new JButton("Return"); 
		
		joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//action 
		joinButton.addActionListener( e -> {
			String targetIp = ipField.getText().trim(); 
			System.out.println("Attempting to connect to: " + targetIp);
			
			try {
				BlackjackClient client = new BlackjackClient(targetIp); 
				
				SwingUtilities.invokeLater(() -> {
					new OnlineGameConnector(client); 
					frame.dispose(); 
				});
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frame, "Could not connect to server at "+ targetIp + "\nMake sure the host has started the server",
						"Connetion Error", JOptionPane.ERROR_MESSAGE); 
			}
		});
		
		returnButton.addActionListener( e -> {
			frame.dispose(); 
			new BlackjackStartWindow(); 
		});
		
		panel.add(Box.createVerticalGlue());
		panel.add(title); 
		panel.add(Box.createRigidArea(new Dimension(0,30)));
		panel.add(promptLabel);
		panel.add(Box.createRigidArea(new Dimension(0,10))); 
		panel.add(ipField); 
		panel.add(Box.createRigidArea(new Dimension(0, 30)));
		panel.add(joinButton); 
		panel.add(Box.createRigidArea(new Dimension(0,15)));
		panel.add(returnButton); 
		panel.add(Box.createVerticalGlue()); 
		
		frame.add(panel);
		frame.setVisible(true);
	}

}
