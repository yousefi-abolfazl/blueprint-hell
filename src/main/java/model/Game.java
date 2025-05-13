package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game {
    private static Game INSTANCE;
    
    private List<NetworkSystem> systems;
    private List<Wire> wires;
    private int remainingWireLength;
    private int temporalProgress;
    private int packetLoss;
    private int coins;
    private boolean isPaused;
    private boolean isGameOver;
    private int currentLevel;
    private boolean impactEffectActive;
    private boolean collisionDisabled;
    
    private Game() {
        systems = new ArrayList<>();
        wires = new ArrayList<>();
        remainingWireLength = 1000; // Default starting wire length
        temporalProgress = 0;
        packetLoss = 0;
        coins = 0;
        isPaused = false;
        isGameOver = false;
        currentLevel = 1;
        impactEffectActive = true;
        collisionDisabled = false;
    }
    
    public static Game getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Game();
        }
        return INSTANCE;
    }
    
    public void addSystem(NetworkSystem system) {
        systems.add(system);
    }
    
    public void addWire(Wire wire) {
        int wireLength = wire.getLength();
        if (wireLength <= remainingWireLength) {
            wires.add(wire);
            remainingWireLength -= wireLength;
        }
    }
    
    public void removeWire(Wire wire) {
        if (wires.remove(wire)) {
            remainingWireLength += wire.getLength();
        }
    }
    
    public void update() {
        if (isPaused || isGameOver) {
            return;
        }
        
        // Update all systems
        for (NetworkSystem system : systems) {
            system.update();
        }
        
        // Handle packet interactions and check for collisions
        checkPacketCollisions();
        
        // Check game over condition
        if (packetLoss > 50) { // 50% loss threshold
            isGameOver = true;
        }
    }
    
    private void checkPacketCollisions() {
        if (collisionDisabled) {
            return;
        }
        
        // For all wires, check if packets on them collide
        for (Wire wire : wires) {
            List<Packet> packets = wire.getPacketsOnWire();
            
            // Check collisions between packets on the same wire
            for (int i = 0; i < packets.size(); i++) {
                Packet packet1 = packets.get(i);
                
                for (int j = i + 1; j < packets.size(); j++) {
                    Packet packet2 = packets.get(j);
                    
                    if (packetsCollide(packet1, packet2)) {
                        handleCollision(packet1, packet2);
                    }
                }
            }
        }
    }
    
    private boolean packetsCollide(Packet packet1, Packet packet2) {
        // Simple collision detection based on distance
        Point pos1 = packet1.getPosition();
        Point pos2 = packet2.getPosition();
        
        int collisionThreshold = packet1.getSize() + packet2.getSize();
        double distance = Math.sqrt(
            Math.pow(pos2.x - pos1.x, 2) + 
            Math.pow(pos2.y - pos1.y, 2)
        );
        
        return distance < collisionThreshold;
    }
    
    private void handleCollision(Packet packet1, Packet packet2) {
        if (!impactEffectActive) {
            return;
        }
        
        // Generate impact effect
        Point collisionPoint = new Point(
            (packet1.getPosition().x + packet2.getPosition().x) / 2,
            (packet1.getPosition().y + packet2.getPosition().y) / 2
        );
        
        // Add noise to both packets
        packet1.addNoise(2);
        packet2.addNoise(2);
        
        // Check if packets are lost due to noise
        checkPacketLoss(packet1);
        checkPacketLoss(packet2);
        
        // Apply impact to nearby packets
        applyImpactToNearbyPackets(collisionPoint, 50); // 50 is the impact radius
    }
    
    private void checkPacketLoss(Packet packet) {
        if (packet.isLost()) {
            // Remove packet from its wire
            for (Wire wire : wires) {
                if (wire.getPacketsOnWire().contains(packet)) {
                    wire.removePacket(packet);
                    packetLoss++;
                    break;
                }
            }
        }
    }
    
    private void applyImpactToNearbyPackets(Point impactPoint, int radius) {
        // For each wire
        for (Wire wire : wires) {
            // For each packet on the wire
            for (Packet packet : wire.getPacketsOnWire()) {
                Point packetPos = packet.getPosition();
                
                // Calculate distance from impact
                double distance = Math.sqrt(
                    Math.pow(packetPos.x - impactPoint.x, 2) +
                    Math.pow(packetPos.y - impactPoint.y, 2)
                );
                
                // If within impact radius, apply force based on distance
                if (distance < radius) {
                    // Calculate force magnitude (stronger when closer)
                    double forceMagnitude = 1.0 - (distance / radius);
                    
                    // Calculate direction away from impact
                    double dx = packetPos.x - impactPoint.x;
                    double dy = packetPos.y - impactPoint.y;
                    
                    // Normalize direction
                    double length = Math.sqrt(dx * dx + dy * dy);
                    if (length > 0) {
                        dx /= length;
                        dy /= length;
                    }
                    
                    // Apply force (move packet)
                    int moveX = (int) (dx * forceMagnitude * 10);
                    int moveY = (int) (dy * forceMagnitude * 10);
                    
                    packet.setPosition(new Point(
                        packetPos.x + moveX,
                        packetPos.y + moveY
                    ));
                    
                    // Check if packet was pushed off the wire
                    if (!wire.isPointNearWire(packet.getPosition(), 10)) {
                        wire.removePacket(packet);
                        packetLoss++;
                    }
                }
            }
        }
    }
    
    public void advanceTime() {
        temporalProgress++;
    }
    
    public void rewindTime() {
        if (temporalProgress > 0) {
            temporalProgress--;
        }
    }
    
    // Power-ups
    public void disableImpact(int duration) {
        impactEffectActive = false;
        // Would need a timer to re-enable after duration
    }
    
    public void disableCollisions(int duration) {
        collisionDisabled = true;
        // Would need a timer to re-enable after duration
    }
    
    public void resetAllPacketNoise() {
        for (Wire wire : wires) {
            for (Packet packet : wire.getPacketsOnWire()) {
                packet.resetNoise();
            }
        }
    }
    
    public void addCoins(int amount) {
        coins += amount;
    }
    
    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }
    
    // Getters and setters
    public List<NetworkSystem> getSystems() {
        return systems;
    }
    
    public List<Wire> getWires() {
        return wires;
    }
    
    public int getRemainingWireLength() {
        return remainingWireLength;
    }
    
    public int getTemporalProgress() {
        return temporalProgress;
    }
    
    public int getPacketLoss() {
        return packetLoss;
    }
    
    public int getCoins() {
        return coins;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void setPaused(boolean paused) {
        isPaused = paused;
    }
    
    public boolean isGameOver() {
        return isGameOver;
    }
    
    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public void setCurrentLevel(int level) {
        currentLevel = level;
    }
    
    public void resetGame() {
        systems.clear();
        wires.clear();
        remainingWireLength = 1000;
        temporalProgress = 0;
        packetLoss = 0;
        isPaused = false;
        isGameOver = false;
        impactEffectActive = true;
        collisionDisabled = false;
    }
} 