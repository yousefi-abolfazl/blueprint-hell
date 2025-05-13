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
        
        // No need to load sounds for now - we'll just use silent versions
        System.out.println("SoundManager initialized with silent sound mode");
    }
    
    public static SoundManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SoundManager();
        }
        return INSTANCE;
    }
    
    public void playSound(String name) {
        // Do nothing - silent mode
        System.out.println("Playing sound (silent mode): " + name);
    }
    
    public void startBackgroundMusic() {
        // Do nothing - silent mode
        System.out.println("Starting background music (silent mode)");
    }
    
    public void stopBackgroundMusic() {
        // Do nothing - silent mode
        System.out.println("Stopping background music (silent mode)");
    }
    
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("Volume set to: " + this.volume + " (silent mode)");
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setMuted(boolean muted) {
        this.muted = muted;
        System.out.println("Mute set to: " + this.muted + " (silent mode)");
    }
    
    public boolean isMuted() {
        return muted;
    }
    
    // Power-up timer methods
    public void scheduleOAtarDeactivation(Runnable onComplete) {
        System.out.println("Scheduling O'Atar deactivation");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onComplete.run();
                timer.cancel();
            }
        }, Constants.O_ATAR_DURATION * 16); // Convert frames to milliseconds (assuming 60 FPS)
    }
    
    public void scheduleOAiryamanDeactivation(Runnable onComplete) {
        System.out.println("Scheduling O'Airyaman deactivation");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onComplete.run();
                timer.cancel();
            }
        }, Constants.O_AIRYAMAN_DURATION * 16); // Convert frames to milliseconds
    }
} 