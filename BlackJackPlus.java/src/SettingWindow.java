import javax.swing.*; 
import java.awt.*; 

public class SettingWindow {
	public SettingWindow () {
		
		//main frame
		JFrame frame = new JFrame("Settings"); 
		frame.setSize(800, 600); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
	}
}
