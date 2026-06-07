package gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CardRenderer {
	 private static URL getCardImageURL(String cardCode) {
	        cardCode = cardCode.trim().toLowerCase().replace("[", "").replace("]", "");
	        if (cardCode.contains("hidden") || cardCode.isEmpty()) {
	        	return CardRenderer.class.getResource("/image/card_back.png");	       
	        }

	        if (cardCode.length() < 2) return null;

	        String valuePart = cardCode.substring(0, cardCode.length() - 1);
	        char suitPart = cardCode.charAt(cardCode.length() - 1);

	        String valueName;
	        switch (valuePart) {
	            case "a":  valueName = "ace"; break;
	            case "j":  valueName = "jack"; break;
	            case "q":  valueName = "queen"; break;
	            case "k":  valueName = "king"; break;
	            default:   valueName = valuePart; break;
	        }

	        String suitName;
	        switch (suitPart) {
	            case 'd': suitName = "diamonds"; break;
	            case 'h': suitName = "hearts"; break;
	            case 'c': suitName = "clubs"; break;
	            case 's': suitName = "spades"; break;
	            default:  suitName = "unknown"; break;
	        }
	        return CardRenderer.class.getResource("/image/" + valueName + "_of_" + suitName + ".png"); 
	    }

	    static void displayCard(JPanel panel, String cardCode) {
	        URL imgURL = getCardImageURL(cardCode);
	        if (imgURL != null) {
	            ImageIcon icon = new ImageIcon(imgURL);
	            JPanel cardShell = new JPanel(new BorderLayout());
	            cardShell.setPreferredSize(new Dimension(65, 90));
	            cardShell.setBackground(Color.WHITE);
	            cardShell.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 1, true));
	            
	            Image scaled = icon.getImage().getScaledInstance(55, 80, Image.SCALE_SMOOTH);
	            JLabel cardLabel = new JLabel(new ImageIcon(scaled));
	            
	            cardShell.add(cardLabel, BorderLayout.CENTER);
	            panel.add(cardShell);
	        } else {
	            JLabel errorLabel = new JLabel("[" + cardCode + "]");
	            errorLabel.setForeground(Color.YELLOW);
	            panel.add(errorLabel);
	        }
	    }

}
