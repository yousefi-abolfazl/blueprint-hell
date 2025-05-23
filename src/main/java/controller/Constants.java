package controller;

import java.awt.*;

public class Constants {
    public static final Dimension GAME_FRAME_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int GRID_CELL_SIZE = 50;
    
    public static final int PACKET_SQUARE_SIZE = 2;
    public static final int PACKET_TRIANGLE_SIZE = 3;
    public static final Color PACKET_SQUARE_COLOR = new Color(25, 118, 210);
    public static final Color PACKET_TRIANGLE_COLOR = new Color(255, 193, 7);
    public static final int PACKET_SQUARE_THICKNESS = 2;
    public static final int PACKET_TRIANGLE_THICKNESS = 2;
    public static final int PACKET_SQUARE_SPEED = 7;
    public static final int PACKET_TRIANGLE_SPEED = 5;
    
    public static final int STANDARD_SYSTEM_STORAGE = 5;
    public static final int SOURCE_SYSTEM_SIZE = 4;
    public static final int DESTINATION_SYSTEM_SIZE = 5;
    public static final int SYSTEM_SIZE = 2;
    public static final Color SOURCE_SYSTEM_COLOR = new Color(255, 100, 100);
    public static final Color DESTINATION_SYSTEM_COLOR = new Color(100, 255, 100);
    public static final Color SYSTEM_COLOR = new Color(100, 100, 255);
    
    public static final int WIRE_THICKNESS = 2;
    public static final Color WIRE_ALLOWABLE_COLOR = Color.GREEN;
    public static final Color WIRE_UNALLOWABLE_COLOR = Color.RED;
    public static final int DEFAULT_REMAINING_WIRE_LENGTH = 1000;
    
    public static final int PORT_SQUARE_SIZE = 1;
    public static final int PORT_TRIANGLE_SIZE = 1;
    public static final Color PORT_INPUT_COLOR = new Color(50, 200, 50);
    public static final Color PORT_OUTPUT_COLOR = new Color(200, 50, 50);
    
    public static final int IMPACT_RADIUS = 50;
    public static final int IMPACT_NOISE_AMOUNT = 1;
    public static final int IMPACT_FORCE_MULTIPLIER = 10;
    public static final int WIRE_PROXIMITY_THRESHOLD = 20;
    public static final int PACKET_LOSS_THRESHOLD = 50;
    
    public static final int O_ATAR_DURATION = 600;
    public static final int O_AIRYAMAN_DURATION = 300;
    
    public static final int O_ATAR_COST = 3;
    public static final int O_AIRYAMAN_COST = 4;
    public static final int O_ANAHITA_COST = 5;
    
    public static final int PACKETS_REQUIRED_PER_DESTINATION = 5;
    
    public static final String SOUND_CONNECTION = "src/main/resources/sounds/connection.wav";
    public static final String SOUND_PACKET_LOST = "src/main/resources/sounds/packet_lost.wav";
    public static final String SOUND_LEVEL_COMPLETE = "src/main/resources/sounds/level_complete.wav";
    public static final String SOUND_GAME_OVER = "src/main/resources/sounds/game_over.wav";
    public static final String SOUND_BACKGROUND = "src/main/resources/sounds/background.wav";
    public static final String SOUND_COLLISION = "src/main/resources/sounds/collision.wav";
    public static final String SOUND_GAME_START = "src/main/resources/sounds/game_start.wav";
    public static final String SOUND_GAME_PAUSE = "src/main/resources/sounds/game_pause.wav";
    public static final String SOUND_POWERUP = "src/main/resources/sounds/powerup.wav";

    public static final boolean DEBUG_PACKET_MOVEMENT = true;
    public static final boolean DEBUG_PACKET_LOSS = true;
    public static final boolean DEBUG_COLLISIONS = true;
}
