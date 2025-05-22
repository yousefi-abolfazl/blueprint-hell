package model;

import controller.Constants;
import controller.SoundManager;

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
    private int totalPacketsGenerated;
    private int totalPacketsLost;
    
    private Game() {
        systems = new ArrayList<>();
        wires = new ArrayList<>();
        remainingWireLength = Constants.DEFAULT_REMAINING_WIRE_LENGTH;
        temporalProgress = 0;
        packetLoss = 0;
        coins = 0;
        isPaused = false;
        isGameOver = false;
        currentLevel = 1;
        impactEffectActive = true;
        collisionDisabled = false;
        totalPacketsGenerated = 0;
        totalPacketsLost = 0;
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
        
        // Update all wires
        for (Wire wire : wires) {
            wire.update();
        }
        
        // Handle packet interactions and check for collisions
        checkPacketCollisions();
        
        // نمایش اطلاعات جاری packet loss هر 30 فریم (حدود 0.5 ثانیه)
        if (temporalProgress % 30 == 0) {
            System.out.println("Current stats - Total packets: " + totalPacketsGenerated + 
                             ", Lost packets: " + totalPacketsLost + 
                             ", Loss percentage: " + packetLoss + "%");
        }
        
        // Check game over condition
        if (packetLoss > Constants.PACKET_LOSS_THRESHOLD) {
            isGameOver = true;
            SoundManager.getInstance().playSound("game_over");
            System.out.println("GAME OVER! Packet loss threshold exceeded: " + packetLoss + "% > " + 
                             Constants.PACKET_LOSS_THRESHOLD + "%");
        }
        
        // Check level completion
        checkLevelCompletion();
        
        // افزایش شمارنده زمان
        temporalProgress++;
    }
    
    private void checkLevelCompletion() {
        int totalPacketsReceived = 0;
        int requiredPackets = 0;
        
        for (NetworkSystem system : systems) {
            if (system instanceof DestinationSystem) {
                totalPacketsReceived += ((DestinationSystem) system).getPacketsReceived();
                requiredPackets += Constants.PACKETS_REQUIRED_PER_DESTINATION;
            }
        }
        
        if (totalPacketsReceived >= requiredPackets && !isGameOver) {
            // Level completed!
            SoundManager.getInstance().playSound("level_complete");
            // Let the SceneController handle level completion via its isLevelComplete method
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
        
        // Check if both packets are on the same wire and moving in the same direction
        if (arePacketsOnSameWire(packet1, packet2)) {
            // For packets on the same wire moving in the same direction, use a reduced collision threshold
            // to make it less likely that they collide
            Port source1 = packet1.getSourcePort();
            Port source2 = packet2.getSourcePort();
            
            // If they're from the same source port (same wire), be more forgiving
            if (source1 == source2) {
                // Calculate direction vectors to see if they're moving in the same direction
                Wire wire = findWireContainingPacket(packet1);
                if (wire != null) {
                    Point wireEnd = wire.getDestinationPort().getPosition();
                    
                    // Calculate direction from packet1 to wire end
                    double dx1 = wireEnd.x - pos1.x;
                    double dy1 = wireEnd.y - pos1.y;
                    
                    // Calculate direction from packet2 to wire end
                    double dx2 = wireEnd.x - pos2.x;
                    double dy2 = wireEnd.y - pos2.y;
                    
                    // Calculate dot product to check if moving in same direction (positive dot product)
                    double dotProduct = dx1 * dx2 + dy1 * dy2;
                    
                    if (dotProduct > 0) {
                        // They're moving in roughly the same direction, so be more forgiving
                        // Calculate distance between them
                        double distance = Math.sqrt(
                            Math.pow(pos2.x - pos1.x, 2) + 
                            Math.pow(pos2.y - pos1.y, 2)
                        );
                        
                        // Use a much smaller collision threshold for packets moving the same direction
                        int reducedCollisionThreshold = (packet1.getSize() + packet2.getSize()) / 2;
                        System.out.println("Same direction packets: Distance=" + distance + 
                                          ", Threshold=" + reducedCollisionThreshold);
                        return distance < reducedCollisionThreshold;
                    }
                }
            }
        }
        
        // Standard collision detection for packets not on same wire or moving in different directions
        int collisionThreshold = packet1.getSize() + packet2.getSize();
        double distance = Math.sqrt(
            Math.pow(pos2.x - pos1.x, 2) + 
            Math.pow(pos2.y - pos1.y, 2)
        );
        
        return distance < collisionThreshold;
    }
    
    private boolean arePacketsOnSameWire(Packet packet1, Packet packet2) {
        for (Wire wire : wires) {
            List<Packet> packets = wire.getPacketsOnWire();
            if (packets.contains(packet1) && packets.contains(packet2)) {
                return true;
            }
        }
        return false;
    }
    
    private Wire findWireContainingPacket(Packet packet) {
        for (Wire wire : wires) {
            if (wire.getPacketsOnWire().contains(packet)) {
                return wire;
            }
        }
        return null;
    }
    
    private void handleCollision(Packet packet1, Packet packet2) {
        if (!impactEffectActive) {
            return;
        }
        
        // Play collision sound
        SoundManager.getInstance().playSound("collision");
        
        // Generate impact effect
        Point collisionPoint = new Point(
            (packet1.getPosition().x + packet2.getPosition().x) / 2,
            (packet1.getPosition().y + packet2.getPosition().y) / 2
        );
        
        System.out.println("Collision between packets at " + collisionPoint.x + "," + collisionPoint.y);
        System.out.println("Packet 1: size=" + packet1.getSize() + ", noise=" + packet1.getNoise());
        System.out.println("Packet 2: size=" + packet2.getSize() + ", noise=" + packet2.getNoise());
        
        // Add noise carefully - ensure we don't immediately lose packets if they're at size-1 noise
        if (packet1.getNoise() + Constants.IMPACT_NOISE_AMOUNT >= packet1.getSize() &&
            packet2.getNoise() + Constants.IMPACT_NOISE_AMOUNT >= packet2.getSize()) {
            // Both packets would be lost - add less noise to one of them randomly
            if (Math.random() < 0.5) {
                // Add full noise to packet1, reduced noise to packet2
                packet1.addNoise(Constants.IMPACT_NOISE_AMOUNT);
                packet2.addNoise(Math.max(0, packet2.getSize() - packet2.getNoise() - 1));
                System.out.println("Reduced noise for packet 2 to prevent simultaneous loss");
            } else {
                // Add full noise to packet2, reduced noise to packet1
                packet2.addNoise(Constants.IMPACT_NOISE_AMOUNT);
                packet1.addNoise(Math.max(0, packet1.getSize() - packet1.getNoise() - 1));
                System.out.println("Reduced noise for packet 1 to prevent simultaneous loss");
            }
        } else {
            // Regular noise addition
            packet1.addNoise(Constants.IMPACT_NOISE_AMOUNT);
            packet2.addNoise(Constants.IMPACT_NOISE_AMOUNT);
        }
        
        System.out.println("After collision - Packet 1: noise=" + packet1.getNoise());
        System.out.println("After collision - Packet 2: noise=" + packet2.getNoise());
        
        // Check if packets are lost due to noise
        checkPacketLoss(packet1);
        checkPacketLoss(packet2);
        
        // Apply impact to nearby packets
        applyImpactToNearbyPackets(collisionPoint, Constants.IMPACT_RADIUS);
    }
    
    private void checkPacketLoss(Packet packet) {
        if (packet.isLost()) {
            // Play packet lost sound
            SoundManager.getInstance().playSound("packet_lost");
            
            // Remove packet from its wire
            for (Wire wire : wires) {
                if (wire.getPacketsOnWire().contains(packet)) {
                    wire.removePacket(packet);
                    totalPacketsLost++;
                    updatePacketLossPercentage();
                    
                    // چاپ وضعیت فعلی packet loss
                    System.out.println("Packet Lost! Total packets: " + totalPacketsGenerated + 
                                     ", Lost packets: " + totalPacketsLost + 
                                     ", Loss percentage: " + packetLoss + "%");
                    
                    break;
                }
            }
        }
    }
    
    private void applyImpactToNearbyPackets(Point impactPoint, int radius) {
        // For each wire
        for (Wire wire : wires) {
            // For each packet on the wire
            for (Packet packet : new ArrayList<>(wire.getPacketsOnWire())) {
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
                    int moveX = (int) (dx * forceMagnitude * Constants.IMPACT_FORCE_MULTIPLIER);
                    int moveY = (int) (dy * forceMagnitude * Constants.IMPACT_FORCE_MULTIPLIER);
                    
                    packet.setPosition(new Point(
                        packetPos.x + moveX,
                        packetPos.y + moveY
                    ));
                    
                    // Check if packet was pushed off the wire
                    if (!wire.isPointNearWire(packet.getPosition(), Constants.WIRE_PROXIMITY_THRESHOLD)) {
                        wire.removePacket(packet);
                        totalPacketsLost++;
                        updatePacketLossPercentage();
                        System.out.println("Packet knocked off wire! Total packets: " + totalPacketsGenerated + 
                                           ", Lost packets: " + totalPacketsLost + 
                                           ", Loss percentage: " + packetLoss + "%");
                        SoundManager.getInstance().playSound("packet_lost");
                    }
                }
            }
        }
    }
    
    public void advanceTime() {
        temporalProgress++;
        System.out.println("Time advanced to: " + temporalProgress);
        
        // اجرای یک سیکل به‌روزرسانی برای همه سیستم‌ها
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem) {
                // دستور به سیستم منبع برای تولید یک پکت
                SourceSystem sourceSystem = (SourceSystem) system;
                sourceSystem.setActive(true);
                sourceSystem.forceGeneratePacket();
            }
        }
    }
    
    public void rewindTime() {
        if (temporalProgress > 0) {
            temporalProgress--;
            System.out.println("Time rewinded to: " + temporalProgress);
            
            // کاهش اثرات noise در همه پکت‌ها
            for (Wire wire : wires) {
                for (Packet packet : wire.getPacketsOnWire()) {
                    // کاهش noise به میزان 1 واحد
                    int currentNoise = packet.getNoise();
                    if (currentNoise > 0) {
                        packet.resetNoise();
                        packet.addNoise(currentNoise - 1);
                    }
                }
            }
        }
    }
    
    // Power-ups
    public void disableImpact(int duration) {
        impactEffectActive = false;
        SoundManager.getInstance().playSound("powerup");
        
        // Schedule re-enabling of impact after duration
        SoundManager.getInstance().scheduleOAtarDeactivation(() -> {
            impactEffectActive = true;
        });
    }
    
    public void disableCollisions(int duration) {
        collisionDisabled = true;
        SoundManager.getInstance().playSound("powerup");
        
        // Schedule re-enabling of collisions after duration
        SoundManager.getInstance().scheduleOAiryamanDeactivation(() -> {
            collisionDisabled = false;
        });
    }
    
    public void resetAllPacketNoise() {
        SoundManager.getInstance().playSound("powerup");
        
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
        remainingWireLength = Constants.DEFAULT_REMAINING_WIRE_LENGTH;
        temporalProgress = 0;
        packetLoss = 0;
        coins = 0;
        isPaused = false;
        isGameOver = false;
        impactEffectActive = true;
        collisionDisabled = false;
        totalPacketsGenerated = 0;
        totalPacketsLost = 0;
    }
    
    public void incrementTotalPacketsGenerated() {
        totalPacketsGenerated++;
        updatePacketLossPercentage();
    }
    
    public void decrementTotalPacketsGenerated() {
        if (totalPacketsGenerated > 0) {
            totalPacketsGenerated--;
            updatePacketLossPercentage();
        }
    }
    
    public int getTotalPacketsGenerated() {
        return totalPacketsGenerated;
    }
    
    public int getTotalPacketsLost() {
        return totalPacketsLost;
    }
    
    private void updatePacketLossPercentage() {
        if (totalPacketsGenerated > 0) {
            int oldPacketLoss = packetLoss;
            packetLoss = (totalPacketsLost * 100) / totalPacketsGenerated;
            
            // Debug output to understand the calculation
            System.out.println("PACKET LOSS CALCULATION: Lost=" + totalPacketsLost + 
                              ", Generated=" + totalPacketsGenerated + 
                              ", Old percentage=" + oldPacketLoss +
                              ", New percentage=" + packetLoss);
                              
            // Check if we just crossed the game over threshold
            if (packetLoss >= Constants.PACKET_LOSS_THRESHOLD && oldPacketLoss < Constants.PACKET_LOSS_THRESHOLD) {
                System.out.println("WARNING: Packet loss has reached " + packetLoss + "%, which exceeds threshold of " + 
                                  Constants.PACKET_LOSS_THRESHOLD + "%");
            }
        } else {
            packetLoss = 0;
        }
    }
} 