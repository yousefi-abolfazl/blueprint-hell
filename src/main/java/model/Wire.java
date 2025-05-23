package model;

import controller.Constants;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Wire {
    private Port sourcePort;
    private Port destinationPort;
    private int length;
    private Color color;
    private List<Packet> packetsOnWire;
    
    public Wire(Port sourcePort, Port destinationPort) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.color = Constants.WIRE_ALLOWABLE_COLOR;
        this.packetsOnWire = new ArrayList<>();
        calculateLength();
    }
    
    private void calculateLength() {
        Point source = sourcePort.getPosition();
        Point destination = destinationPort.getPosition();
        this.length = (int) Math.sqrt(
            Math.pow(destination.x - source.x, 2) + 
            Math.pow(destination.y - source.y, 2)
        );
    }
    
    public void render(Graphics2D g) {
        Point source = sourcePort.getPosition();
        Point destination = destinationPort.getPosition();
        
        Stroke originalStroke = g.getStroke();
        Color originalColor = g.getColor();
        
        if (!packetsOnWire.isEmpty()) {
            g.setColor(new Color(100, 200, 255, 220));
            g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS + 2));
            g.drawLine(source.x, source.y, destination.x, destination.y);
            
            g.setColor(new Color(100, 200, 255, 80));
            g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS + 4));
            g.drawLine(source.x, source.y, destination.x, destination.y);
        }
        
        g.setColor(color);
        g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS));
        g.drawLine(source.x, source.y, destination.x, destination.y);
        
        g.setStroke(originalStroke);
        g.setColor(originalColor);
        
        double totalLength = Math.sqrt(Math.pow(destination.x - source.x, 2) + Math.pow(destination.y - source.y, 2));
        if (totalLength > 0) {
            double stepSize = 20;
            double dx = (destination.x - source.x) / totalLength;
            double dy = (destination.y - source.y) / totalLength;
            
            for (double step = 0; step < totalLength; step += stepSize) {
                int dotX = (int)(source.x + dx * step);
                int dotY = (int)(source.y + dy * step);
                g.setColor(Color.DARK_GRAY);
                g.fillOval(dotX - 1, dotY - 1, 3, 3);
            }
        }
        
        for (Packet packet : packetsOnWire) {
            packet.render(g);
        }
    }
    
    public void update() {
        if (Constants.DEBUG_PACKET_MOVEMENT) {
            System.out.println("\n----- WIRE UPDATE: " + packetsOnWire.size() + " packets -----");
            if (!packetsOnWire.isEmpty()) {
                System.out.println("  From: " + sourcePort.getPosition().x + "," + sourcePort.getPosition().y);
                System.out.println("  To: " + destinationPort.getPosition().x + "," + destinationPort.getPosition().y);
                System.out.println("  Wire length: " + length);
            }
        }

        for (Packet packet : new ArrayList<>(packetsOnWire)) {
            if (Constants.DEBUG_PACKET_MOVEMENT) {
                System.out.println("  Packet before update: " + packet.getPosition().x + "," + packet.getPosition().y);
            }
            
            if (!packet.isMoving()) {
                if (Constants.DEBUG_PACKET_MOVEMENT) {
                    System.out.println("  Starting packet movement on wire from " + sourcePort.getPosition().x + "," + sourcePort.getPosition().y +
                                  " to " + destinationPort.getPosition().x + "," + destinationPort.getPosition().y);
                }
                
                if (packet instanceof TrianglePacket) {
                    packet.currentSpeed = 1.5;
                }
                
                packet.startMoving(destinationPort.getPosition(), sourcePort);
            }
            
            packet.update();
            
            if (Constants.DEBUG_PACKET_MOVEMENT) {
                System.out.println("  Packet after update: " + packet.getPosition().x + "," + packet.getPosition().y + 
                              ", isMoving: " + packet.isMoving() + ", speed: " + packet.getCurrentSpeed());
            }
            
            Point packetPos = packet.getPosition();
            
            if (packet.isLost()) {
                if (Constants.DEBUG_PACKET_LOSS) {
                    System.out.println("  Packet lost check in Wire.update() - calling Game.checkPacketLoss()");
                }
                Game.getInstance().checkPacketLoss(packet);
                continue;
            }
            
            double distanceToTarget = Math.sqrt(
                Math.pow(destinationPort.getPosition().x - packet.getPosition().x, 2) + 
                Math.pow(destinationPort.getPosition().y - packet.getPosition().y, 2)
            );
            
            if (Constants.DEBUG_PACKET_MOVEMENT) {
                System.out.println("  Distance to destination: " + distanceToTarget);
            }
            
            if (distanceToTarget < 10) {
                if (Constants.DEBUG_PACKET_MOVEMENT) {
                    System.out.println("  PACKET REACHED DESTINATION!");
                }
                packet.setPosition(new Point(destinationPort.getPosition()));
                destinationPort.getParentSystem().receivePacket(packet);
                removePacket(packet);
            }
        }
        
        if (Constants.DEBUG_PACKET_MOVEMENT && !packetsOnWire.isEmpty()) {
            System.out.println("----- WIRE UPDATE COMPLETE: " + packetsOnWire.size() + " packets remain -----\n");
        }
    }
    
    public boolean isPointNearWire(Point point, int threshold) {
        Point source = sourcePort.getPosition();
        Point destination = destinationPort.getPosition();
        
        double normalLength = Math.sqrt(
            Math.pow(destination.x - source.x, 2) + 
            Math.pow(destination.y - source.y, 2)
        );
        
        double distance = Math.abs(
            (destination.y - source.y) * point.x - 
            (destination.x - source.x) * point.y + 
            destination.x * source.y - 
            destination.y * source.x
        ) / normalLength;
        
        return distance <= threshold;
    }
    
    public void addPacket(Packet packet) {
        packet.setSourcePort(sourcePort);
        
        if (!packetsOnWire.isEmpty()) {
            Packet lastPacket = packetsOnWire.get(packetsOnWire.size() - 1);
            Point lastPosition = lastPacket.getPosition();
            
            Point source = sourcePort.getPosition();
            Point destination = destinationPort.getPosition();
            
            double totalDistance = Math.sqrt(
                Math.pow(destination.x - source.x, 2) + 
                Math.pow(destination.y - source.y, 2)
            );
            
            double dx = (destination.x - source.x) / totalDistance;
            double dy = (destination.y - source.y) / totalDistance;
            
            double distanceMoved = Math.sqrt(
                Math.pow(lastPosition.x - source.x, 2) + 
                Math.pow(lastPosition.y - source.y, 2)
            );
            
            if (distanceMoved < totalDistance * 0.25) {
                int offset = 20;
                int newX = source.x + (int)(dx * offset);
                int newY = source.y + (int)(dy * offset);
                packet.setPosition(new Point(newX, newY));
                System.out.println("Offsetting new packet to avoid collision: " + newX + "," + newY);
            }
        }
        
        packetsOnWire.add(packet);
        
        if (packet instanceof TrianglePacket) {
            packet.currentSpeed = 1.5;
        }
        
        packet.startMoving(destinationPort.getPosition(), sourcePort);
    }
    
    public void removePacket(Packet packet) {
        packetsOnWire.remove(packet);
    }
    
    public boolean isEmpty() {
        return packetsOnWire.isEmpty();
    }
    
    public Port getSourcePort() {
        return sourcePort;
    }
    
    public Port getDestinationPort() {
        return destinationPort;
    }
    
    public int getLength() {
        return length;
    }
    
    public List<Packet> getPacketsOnWire() {
        return packetsOnWire;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
} 