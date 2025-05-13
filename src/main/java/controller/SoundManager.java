package controller;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager INSTANCE;
    
    private Map<String, Clip> soundClips;
    private Clip backgroundMusic;
    private float volume;
    private boolean muted;
    
    private SoundManager() {
        soundClips = new HashMap<>();
        volume = 1.0f;
        muted = false;
        
        // Attempt to load sounds
        try {
            loadSounds();
        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
        }
    }
    
    public static SoundManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SoundManager();
        }
        return INSTANCE;
    }
    
    private void loadSounds() {
        // Create sounds directory if it doesn't exist
        File soundsDir = new File("src/main/resources/sounds");
        if (!soundsDir.exists()) {
            soundsDir.mkdirs();
        }
        
        // Load sound effects here when files are available
        // loadSound("collision", "src/main/resources/sounds/collision.wav");
        // loadSound("packet_lost", "src/main/resources/sounds/packet_lost.wav");
        // loadSound("level_complete", "src/main/resources/sounds/level_complete.wav");
        // loadSound("connection", "src/main/resources/sounds/connection.wav");
        
        // Load background music
        // loadBackgroundMusic("src/main/resources/sounds/background.wav");
    }
    
    private void loadSound(String name, String path) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            soundClips.put(name, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound file " + path + ": " + e.getMessage());
        }
    }
    
    private void loadBackgroundMusic(String path) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(path));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading background music: " + e.getMessage());
        }
    }
    
    public void playSound(String name) {
        if (muted) return;
        
        Clip clip = soundClips.get(name);
        if (clip != null) {
            clip.setFramePosition(0);
            
            // Apply volume control
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
            } catch (Exception e) {
                System.err.println("Could not set volume for clip: " + e.getMessage());
            }
            
            clip.start();
        }
    }
    
    public void startBackgroundMusic() {
        if (backgroundMusic != null && !muted) {
            backgroundMusic.setFramePosition(0);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            
            // Apply volume control
            try {
                FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume * 0.7f) + gainControl.getMinimum();  // Background music softer than effects
                gainControl.setValue(gain);
            } catch (Exception e) {
                System.err.println("Could not set volume for background music: " + e.getMessage());
            }
            
            backgroundMusic.start();
        }
    }
    
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
    
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        
        // Update volume for background music if playing
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            try {
                FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * this.volume * 0.7f) + gainControl.getMinimum();
                gainControl.setValue(gain);
            } catch (Exception e) {
                System.err.println("Could not update volume for background music: " + e.getMessage());
            }
        }
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopBackgroundMusic();
        } else {
            startBackgroundMusic();
        }
    }
    
    public boolean isMuted() {
        return muted;
    }
} 