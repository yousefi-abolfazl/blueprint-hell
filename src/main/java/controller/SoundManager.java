package controller;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SoundManager {
    private static SoundManager INSTANCE;
    
    private Map<String, Clip> soundClips;
    private Clip backgroundMusic;
    private float volume;
    private boolean muted;
    
    private SoundManager() {
        soundClips = new HashMap<>();
        volume = 0.5f; // Default volume at 50%
        muted = false;
        
        loadSounds();
    }
    
    private void loadSounds() {
        try {
            // Check if sounds directory exists
            File soundsDir = new File("src/main/resources/sounds");
            if (!soundsDir.exists() || !soundsDir.isDirectory()) {
                System.out.println("Sounds directory not found. Running in silent mode.");
                return;
            }
            
            // Load all sound effects
            tryLoadSound("connection", Constants.SOUND_CONNECTION);
            tryLoadSound("packet_lost", Constants.SOUND_PACKET_LOST);
            tryLoadSound("level_complete", Constants.SOUND_LEVEL_COMPLETE);
            tryLoadSound("game_over", Constants.SOUND_GAME_OVER);
            tryLoadSound("collision", Constants.SOUND_COLLISION);
            tryLoadSound("game_start", Constants.SOUND_GAME_START);
            tryLoadSound("game_pause", Constants.SOUND_GAME_PAUSE);
            tryLoadSound("powerup", Constants.SOUND_POWERUP);
            
            // Try to load background music
            tryLoadBackgroundMusic();
            
        } catch (Exception e) {
            System.out.println("Running in silent mode due to sound initialization error: " + e.getMessage());
        }
    }
    
    private void tryLoadSound(String name, String path) {
        try {
            File soundFile = new File(path);
            if (soundFile.exists() && soundFile.length() > 0) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                
                // Set initial volume
                setClipVolume(clip, volume);
                
                soundClips.put(name, clip);
            } else {
                System.out.println("Sound file not found or empty: " + path);
            }
        } catch (Exception e) {
            System.out.println("Couldn't load sound " + name + ": " + e.getMessage());
        }
    }
    
    private void tryLoadBackgroundMusic() {
        try {
            File musicFile = new File(Constants.SOUND_BACKGROUND);
            if (musicFile.exists() && musicFile.length() > 0) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(ais);
                
                // Set background music to loop continuously
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                
                // Set initial volume
                setClipVolume(backgroundMusic, volume);
            } else {
                System.out.println("Background music file not found or empty: " + Constants.SOUND_BACKGROUND);
            }
        } catch (Exception e) {
            System.out.println("Couldn't load background music: " + e.getMessage());
        }
    }
    
    private void setClipVolume(Clip clip, float volume) {
        if (clip == null) return;
        
        FloatControl gainControl = 
            (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (range * volume) + gainControl.getMinimum();
        gainControl.setValue(gain);
    }
    
    public static SoundManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SoundManager();
        }
        return INSTANCE;
    }
    
    public void playSound(String name) {
        if (muted) return;
        
        Clip clip = soundClips.get(name);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        } else {
            System.err.println("Sound not found: " + name);
        }
    }
    
    public void startBackgroundMusic() {
        if (backgroundMusic != null && !muted) {
            backgroundMusic.setFramePosition(0);
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
        
        // Update volume for all clips
        for (Clip clip : soundClips.values()) {
            setClipVolume(clip, this.volume);
        }
        
        // Update volume for background music
        if (backgroundMusic != null) {
            setClipVolume(backgroundMusic, this.volume);
        }
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setMuted(boolean muted) {
        this.muted = muted;
        
        if (muted) {
            stopBackgroundMusic();
        } else if (backgroundMusic != null) {
            backgroundMusic.start();
        }
    }
    
    public boolean isMuted() {
        return muted;
    }
    
    // Power-up timer methods
    public void scheduleOAtarDeactivation(Runnable onComplete) {
        System.out.println("Scheduling O'Atar deactivation for " + Constants.O_ATAR_DURATION + " frames (10 seconds)");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("O'Atar power-up expired");
                onComplete.run();
                timer.cancel();
            }
        }, Constants.O_ATAR_DURATION * 1000 / 60); // Convert frames to milliseconds (assuming 60 FPS)
    }
    
    public void scheduleOAiryamanDeactivation(Runnable onComplete) {
        System.out.println("Scheduling O'Airyaman deactivation for " + Constants.O_AIRYAMAN_DURATION + " frames (5 seconds)");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("O'Airyaman power-up expired");
                onComplete.run();
                timer.cancel();
            }
        }, Constants.O_AIRYAMAN_DURATION * 1000 / 60); // Convert frames to milliseconds
    }
} 