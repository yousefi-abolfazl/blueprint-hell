# Blueprint Hell

Blueprint Hell is a network simulation game where players must connect different systems and manage packet flow between sources and destinations.

## How to Run

1. Compile the Java files:
```
cd src/main/java
javac blueprinthell/Main.java
```

2. Run the game:
```
java blueprinthell.Main
```

## Game Controls

### Building Phase
- **Mouse**: Click and drag from output ports (RED) to input ports (GREEN) to create connections
- **Space**: Start/pause the simulation
- **Right Arrow**: Advance time (when paused)
- **Left Arrow**: Rewind time (when paused)

### During Gameplay
- **H**: Toggle HUD visibility
- **S**: Open shop (only available during gameplay)
- **Escape**: Stop the simulation

## Game Objectives

1. Connect network systems using wires to create paths for packets
2. Start the simulation by pressing the Space key
3. Guide packets from source systems to destination systems
4. Avoid packet collisions which lead to packet loss
5. Complete the level by delivering 5 packets to each destination system
6. Keep packet loss below 50% to avoid game over

## Power-ups (available in the shop)

- **O'Atar**: Disables the impact effect for 10 seconds (3 coins)
- **O'Airyaman**: Disables collisions for 5 seconds (4 coins)
- **O'Anahita**: Resets noise on all packets (5 coins)

## Known Issues and Troubleshooting

1. If sound files are missing, the game will create placeholders and continue without sound.
2. If you experience focus issues with keyboard controls, click on the game panel to regain focus.
3. Make sure you have Java installed on your system.

## Developer Information

This game follows the MVC (Model-View-Controller) architecture:
- **Model**: Game logic and data classes (Game, NetworkSystem, Port, Packet, etc.)
- **View**: UI components (GamePanel, MainMenuView, etc.)
- **Controller**: Game flow and interaction handling (SceneController, SoundManager)

## Credits

Created as an Advanced Programming project. 