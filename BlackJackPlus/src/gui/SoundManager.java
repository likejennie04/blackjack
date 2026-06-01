package gui;

import javax.sound.sampled.*;
import java.net.URL; 

public class SoundManager {
	private static Clip backgroundMusic; 
	private static Clip buttonOne; 
	private static Clip buttonTwo; 
	private static Clip hitButton; 
	private static Clip standButton; 
	
	public static void init () {
		loadBackgroundMusic("backgroundmusic1.wav"); 
		buttonOne = loadSoundEffect("button1.wav", 0.0f); 
		buttonTwo = loadSoundEffect("button2.wav", 0.0f);    
		hitButton= loadSoundEffect("hitbutton.wav", 0.0f); 
		standButton = loadSoundEffect("standbutton.wav", 0.0f); 

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
	
	private static Clip loadSoundEffect(String fileName, float volume) {
		try { 
			URL url = SoundManager.class.getResource("/sound/" + fileName); 
			if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                setClipVolume(clip, volume); 
                return clip;
            }
        } catch (Exception e) {
            System.err.println("Error loading sound effect [" + fileName + "]: " + e.getMessage());
        }
		return null;
	}

	public static void buttonOne() {
		triggerClip(buttonOne);
	}
	
	public static void buttonTwo() {
		triggerClip(buttonTwo);
	}
	public static void hitButton() {
		triggerClip(hitButton); 
	}
	public static void standButton() {
		triggerClip(standButton); 
	}
	
	
	private static void triggerClip(Clip clip) {
		if (clip != null) {
			clip.setFramePosition(0); 
			clip.start(); 
		}
	}
	
	private static void setClipVolume (Clip clip, float decibels) {
		if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN); 
			gainControl.setValue(decibels);
		}
	}
}