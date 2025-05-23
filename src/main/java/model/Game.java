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
    private int totalPacketsDelivered;
    
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
        totalPacketsDelivered = 0;
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
        
        
        System.out.println("\n===== GAME STATUS UPDATE =====");
        System.out.println("Total packets generated: " + totalPacketsGenerated);
        System.out.println("Total packets delivered: " + totalPacketsDelivered);
        System.out.println("Total packets lost: " + totalPacketsLost);
        System.out.println("Current packet loss: " + packetLoss + "%");
        System.out.println("============================\n");
        
        
        for (NetworkSystem system : systems) {
            system.update();
        }
        
        
        int packetsInTransit = 0;
        for (Wire wire : wires) {
            packetsInTransit += wire.getPacketsOnWire().size();
        }
        System.out.println("Packets currently in transit: " + packetsInTransit);
        
        
        for (Wire wire : wires) {
            wire.update();
        }
        
        
        checkPacketCollisions();
        
        
        if (temporalProgress % 30 == 0) {
            System.out.println("Current stats - Total packets: " + totalPacketsGenerated + 
                             ", Delivered: " + totalPacketsDelivered +
                             ", Lost packets: " + totalPacketsLost + 
                             ", Loss percentage: " + packetLoss + "%");
        }
        
        
        if (packetLoss > Constants.PACKET_LOSS_THRESHOLD) {
            isGameOver = true;
            SoundManager.getInstance().playSound("game_over");
            System.out.println("GAME OVER! Packet loss threshold exceeded: " + packetLoss + "% > " + 
                             Constants.PACKET_LOSS_THRESHOLD + "%");
        }
        
        
        checkLevelCompletion();
        
        
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
            
            SoundManager.getInstance().playSound("level_complete");
            
        }
    }
    
    private void checkPacketCollisions() {
        if (collisionDisabled) {
            return;
        }
        
        
        for (Wire wire : wires) {
            List<Packet> packets = wire.getPacketsOnWire();
            
            
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
        
        Point pos1 = packet1.getPosition();
        Point pos2 = packet2.getPosition();
        
        
        if (arePacketsOnSameWire(packet1, packet2)) {
            
            
            Port source1 = packet1.getSourcePort();
            Port source2 = packet2.getSourcePort();
            
            
            if (source1 == source2) {
                
                Wire wire = findWireContainingPacket(packet1);
                if (wire != null) {
                    Point wireEnd = wire.getDestinationPort().getPosition();
                    
                    
                    double dx1 = wireEnd.x - pos1.x;
                    double dy1 = wireEnd.y - pos1.y;
                    
                    
                    double dx2 = wireEnd.x - pos2.x;
                    double dy2 = wireEnd.y - pos2.y;
                    
                    
                    double dotProduct = dx1 * dx2 + dy1 * dy2;
                    
                    if (dotProduct > 0) {
                        
                        
                        double distance = Math.sqrt(
                            Math.pow(pos2.x - pos1.x, 2) + 
                            Math.pow(pos2.y - pos1.y, 2)
                        );
                        
                        
                        int reducedCollisionThreshold = (packet1.getSize() + packet2.getSize()) / 2;
                        System.out.println("Same direction packets: Distance=" + distance + 
                                          ", Threshold=" + reducedCollisionThreshold);
                        return distance < reducedCollisionThreshold;
                    }
                }
            }
        }
        
        
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
        
        if (Constants.DEBUG_COLLISIONS) {
            System.out.println("\n*** COLLISION DETECTED ***");
            System.out.println("Packet 1: pos=" + packet1.getPosition().x + "," + packet1.getPosition().y + 
                              " size=" + packet1.getSize() + " noise=" + packet1.getNoise());
            System.out.println("Packet 2: pos=" + packet2.getPosition().x + "," + packet2.getPosition().y + 
                              " size=" + packet2.getSize() + " noise=" + packet2.getNoise());
        }
        
        
        SoundManager.getInstance().playSound("collision");
        
        
        Point collisionPoint = new Point(
            (packet1.getPosition().x + packet2.getPosition().x) / 2,
            (packet1.getPosition().y + packet2.getPosition().y) / 2
        );
        
        
        if (packet1.getNoise() + Constants.IMPACT_NOISE_AMOUNT >= packet1.getSize() &&
            packet2.getNoise() + Constants.IMPACT_NOISE_AMOUNT >= packet2.getSize()) {
            
            if (Math.random() < 0.5) {
                
                packet1.addNoise(Constants.IMPACT_NOISE_AMOUNT);
                packet2.addNoise(Math.max(0, packet2.getSize() - packet2.getNoise() - 1));
                if (Constants.DEBUG_COLLISIONS) {
                    System.out.println("Reduced noise for packet 2 to prevent simultaneous loss");
                }
            } else {
                
                packet2.addNoise(Constants.IMPACT_NOISE_AMOUNT);
                packet1.addNoise(Math.max(0, packet1.getSize() - packet1.getNoise() - 1));
                if (Constants.DEBUG_COLLISIONS) {
                    System.out.println("Reduced noise for packet 1 to prevent simultaneous loss");
                }
            }
        } else {
            
            packet1.addNoise(Constants.IMPACT_NOISE_AMOUNT);
            packet2.addNoise(Constants.IMPACT_NOISE_AMOUNT);
            if (Constants.DEBUG_COLLISIONS) {
                System.out.println("Added normal impact noise to both packets");
            }
        }
        
        if (Constants.DEBUG_COLLISIONS) {
            System.out.println("After collision - Packet 1: noise=" + packet1.getNoise());
            System.out.println("After collision - Packet 2: noise=" + packet2.getNoise());
        }
        
        
        checkPacketLoss(packet1);
        checkPacketLoss(packet2);
        
        
        applyImpactToNearbyPackets(collisionPoint, Constants.IMPACT_RADIUS);
        
        
        forceUpdatePacketLoss();
        
        if (Constants.DEBUG_COLLISIONS) {
            System.out.println("*** END OF COLLISION HANDLING ***\n");
        }
    }
    
    public void checkPacketLoss(Packet packet) {
        if (packet.isLost()) {
            
            SoundManager.getInstance().playSound("packet_lost");
            
            
            for (Wire wire : wires) {
                if (wire.getPacketsOnWire().contains(packet)) {
                    wire.removePacket(packet);
                    totalPacketsLost++;
                    
                    
                    System.out.println("===== PACKET LOST =====");
                    System.out.println("Packet lost due to noise: " + packet.getNoise() + " >= " + packet.getSize());
                    System.out.println("Total generated: " + totalPacketsGenerated);
                    System.out.println("Total lost: " + totalPacketsLost);
                    System.out.println("Total delivered: " + totalPacketsDelivered);
                    
                    
                    updatePacketLossPercentage();
                    
                    System.out.println("=====================");
                    break;
                }
            }
        }
    }
    
    private void applyImpactToNearbyPackets(Point impactPoint, int radius) {
        
        for (Wire wire : wires) {
            
            for (Packet packet : new ArrayList<>(wire.getPacketsOnWire())) {
                Point packetPos = packet.getPosition();
                
                
                double distance = Math.sqrt(
                    Math.pow(packetPos.x - impactPoint.x, 2) +
                    Math.pow(packetPos.y - impactPoint.y, 2)
                );
                
                
                if (distance < radius) {
                    
                    double forceMagnitude = 1.0 - (distance / radius);
                    
                    
                    double dx = packetPos.x - impactPoint.x;
                    double dy = packetPos.y - impactPoint.y;
                    
                    
                    double length = Math.sqrt(dx * dx + dy * dy);
                    if (length > 0) {
                        dx /= length;
                        dy /= length;
                    }
                    
                    
                    int moveX = (int) (dx * forceMagnitude * Constants.IMPACT_FORCE_MULTIPLIER);
                    int moveY = (int) (dy * forceMagnitude * Constants.IMPACT_FORCE_MULTIPLIER);
                    
                    packet.setPosition(new Point(
                        packetPos.x + moveX,
                        packetPos.y + moveY
                    ));
                    
                    
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
        
        
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem) {
                
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
            
            
            for (Wire wire : wires) {
                for (Packet packet : wire.getPacketsOnWire()) {
                    
                    int currentNoise = packet.getNoise();
                    if (currentNoise > 0) {
                        packet.resetNoise();
                        packet.addNoise(currentNoise - 1);
                    }
                }
            }
        }
    }
    
    
    public void disableImpact(int duration) {
        impactEffectActive = false;
        SoundManager.getInstance().playSound("powerup");
        
        
        SoundManager.getInstance().scheduleOAtarDeactivation(() -> {
            impactEffectActive = true;
        });
    }
    
    public void disableCollisions(int duration) {
        collisionDisabled = true;
        SoundManager.getInstance().playSound("powerup");
        
        
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
        totalPacketsDelivered = 0;
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
    
    public int getTotalPacketsDelivered() {
        return totalPacketsDelivered;
    }
    
    private void updatePacketLossPercentage() {
        int oldPacketLoss = packetLoss;
        
        
        int processedPackets = totalPacketsLost + totalPacketsDelivered;
        
        if (processedPackets > 0) {
            
            packetLoss = (totalPacketsLost * 100) / processedPackets;
            
            
            System.out.println("PACKET LOSS CALCULATION: Lost=" + totalPacketsLost + 
                              ", Delivered=" + totalPacketsDelivered + 
                              ", Total processed=" + processedPackets +
                              ", Total generated=" + totalPacketsGenerated +
                              ", Old percentage=" + oldPacketLoss +
                              ", New percentage=" + packetLoss);
                              
            
            if (packetLoss >= Constants.PACKET_LOSS_THRESHOLD && oldPacketLoss < Constants.PACKET_LOSS_THRESHOLD) {
                System.out.println("WARNING: Packet loss has reached " + packetLoss + "%, which exceeds threshold of " + 
                                  Constants.PACKET_LOSS_THRESHOLD + "%");
            }
        } else {
            packetLoss = 0;
        }
    }
    
    
    public void incrementPacketsDelivered() {
        totalPacketsDelivered++;
        updatePacketLossPercentage();
        System.out.println("Packet successfully delivered! Total delivered: " + totalPacketsDelivered);
    }
    
    
    public void forceUpdatePacketLoss() {
        updatePacketLossPercentage();
        
        
        System.out.println("\n====== PACKET LOSS DIAGNOSTICS ======");
        System.out.println("Total packets generated: " + totalPacketsGenerated);
        System.out.println("Total packets delivered: " + totalPacketsDelivered);
        System.out.println("Total packets lost: " + totalPacketsLost);
        System.out.println("Total processed packets: " + (totalPacketsDelivered + totalPacketsLost));
        System.out.println("Packets in transit: " + (totalPacketsGenerated - totalPacketsDelivered - totalPacketsLost));
        System.out.println("Current packet loss: " + packetLoss + "%");
        System.out.println("====================================\n");
    }
    
    
    public void incrementPacketsLost() {
        totalPacketsLost++;
        updatePacketLossPercentage();
        System.out.println("Packet lost! Total lost: " + totalPacketsLost);
    }
} 