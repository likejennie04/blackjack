package gui;

import javax.sound.sampled.*;
import java.net.URL; 

public class SoundManager {
	private static Clip backgroundMusic; 
	private static Clip buttonClickSound; 
	
	public static void init () {
		loadBackgroundMusic("backgroundmusic1.wav"); 
		loadButtonClickSound("button1.wav"); 
	}
	
	private static void loadBackgroundMusic (String fileName) {
		try {
			URL url = SoundManager.class.getResource("/sound/" + fileName);
			if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioIn);
                setClipVolume(backgroundMusic, -20.0f); // Softer background music
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
		}
	}
	
	private static void loadButtonClickSound(String fileName) {
		try { 
			URL url = SoundManager.class.getResource("/sound/" + fileName); 
			if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                buttonClickSound = AudioSystem.getClip();
                buttonClickSound.open(audioIn);
                setClipVolume(buttonClickSound, 0.0f); // Louder button clicks
            }
        } catch (Exception e) {
            System.err.println("Error loading click sound: " + e.getMessage());
        }
	}
	
	public static void playClick() {
		if (buttonClickSound != null) {
			buttonClickSound.setFramePosition(0); 
			buttonClickSound.start(); 
		}
	}
	
	private static void setClipVolume (Clip clip, float decibels) {
		if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN); 
			gainControl.setValue(decibels);
		}
	}

}
