package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class AvatarManager {
    private static ArrayList<ImageIcon> avatars = new ArrayList<>();
    private static final int SIZE = 80;

    static {
       
        avatars.add(createSKKUAvatar());
       
        avatars.add(createKoreaFlagAvatar());
       
        avatars.add(createFriesAvatar());
    }

    private static ImageIcon createSKKUAvatar() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
       
        g2.setColor(new Color(0, 102, 51)); 
        g2.fillOval(0, 0, SIZE, SIZE);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Serif", Font.BOLD, 30));
        g2.drawString("SKKU", 5, 52);
        
        g2.dispose();
        return new ImageIcon(img);
    }

    private static ImageIcon createKoreaFlagAvatar() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(Color.WHITE);
        g2.fillOval(0, 0, SIZE, SIZE);
        
        
        g2.setColor(Color.RED);
        g2.fillArc(15, 15, 50, 50, 0, 180);
        g2.setColor(Color.BLUE);
        g2.fillArc(15, 15, 50, 50, 180, 180);
        
        g2.dispose();
        return new ImageIcon(img);
    }

    private static ImageIcon createFriesAvatar() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
       
        g2.setColor(Color.RED);
        g2.fillRect(20, 40, 40, 35);
        
        
        g2.setColor(Color.YELLOW);
        g2.fillRect(25, 15, 8, 30);
        g2.fillRect(36, 10, 8, 35);
        g2.fillRect(47, 15, 8, 30);
        
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ArrayList<ImageIcon> getAllAvatars() {
        return avatars;
    }
}