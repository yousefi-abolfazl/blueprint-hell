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
        // Using Euclidean distance for straight line
        this.length = (int) Math.sqrt(
            Math.pow(destination.x - source.x, 2) + 
            Math.pow(destination.y - source.y, 2)
        );
    }
    
    public void render(Graphics2D g) {
        Point source = sourcePort.getPosition();
        Point destination = destinationPort.getPosition();
        
        // ذخیره‌سازی وضعیت فعلی گرافیک
        Stroke originalStroke = g.getStroke();
        Color originalColor = g.getColor();
        
        // رندر وایر به صورت متفاوت وقتی پکت روی آن است
        if (!packetsOnWire.isEmpty()) {
            // وایر با پکت - نمایش با یک تابش نور آبی کمرنگ
            g.setColor(new Color(100, 200, 255, 220)); // آبی روشن با شفافیت بیشتر
            g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS + 2)); // ضخیم‌تر کردن سیم با پکت
            g.drawLine(source.x, source.y, destination.x, destination.y);
            
            // Add pulsing effect - draw a wider, more transparent line
            g.setColor(new Color(100, 200, 255, 80));
            g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS + 4));
            g.drawLine(source.x, source.y, destination.x, destination.y);
        }
        
        // رندر معمولی وایر
        g.setColor(color);
        g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS));
        g.drawLine(source.x, source.y, destination.x, destination.y);
        
        // بازگرداندن وضعیت اصلی گرافیک
        g.setStroke(originalStroke);
        g.setColor(originalColor);
        
        // DEBUG: Draw dots every 20 pixels along the wire to visualize path
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
        
        // رندر همه پکت‌های روی وایر - with minimal design
        for (Packet packet : packetsOnWire) {
            // No highlight, just render the packet itself
            packet.render(g);
        }
    }
    
    public void update() {
        // Update all packets on the wire using their physics-based movement
        for (Packet packet : new ArrayList<>(packetsOnWire)) {
            // Debug packet position before update
            System.out.println("Packet before update: " + packet.getPosition().x + "," + packet.getPosition().y);
            
            // If the packet is not moving, start moving it toward the destination
            if (!packet.isMoving()) {
                System.out.println("Starting packet movement on wire from " + sourcePort.getPosition().x + "," + sourcePort.getPosition().y +
                                  " to " + destinationPort.getPosition().x + "," + destinationPort.getPosition().y);
                packet.startMoving(destinationPort.getPosition(), sourcePort);
            }
            
            // Now update the packet's position based on physics
            packet.update();
            
            // Debug packet position after update
            System.out.println("Packet after update: " + packet.getPosition().x + "," + packet.getPosition().y + 
                              ", isMoving: " + packet.isMoving() + ", speed: " + packet.getCurrentSpeed());
            
            // Check if packet has reached destination
            double distanceToTarget = Math.sqrt(
                Math.pow(destinationPort.getPosition().x - packet.getPosition().x, 2) + 
                Math.pow(destinationPort.getPosition().y - packet.getPosition().y, 2)
            );
            
            System.out.println("Distance to destination: " + distanceToTarget);
            
            // Use a more reliable way to detect arrival - either very close or stopped moving
            if (distanceToTarget < 10) {
                System.out.println("Packet reached destination port!");
                // Set position exactly to destination to avoid floating point errors
                packet.setPosition(new Point(destinationPort.getPosition()));
                destinationPort.getParentSystem().receivePacket(packet);
                removePacket(packet);
            }
        }
    }
    
    public boolean isPointNearWire(Point point, int threshold) {
        Point source = sourcePort.getPosition();
        Point destination = destinationPort.getPosition();
        
        // Calculate the distance from point to line
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
        
        // Check if there are already packets on this wire
        if (!packetsOnWire.isEmpty()) {
            // Create a small offset to avoid immediate collisions
            // Get the last packet's position
            Packet lastPacket = packetsOnWire.get(packetsOnWire.size() - 1);
            Point lastPosition = lastPacket.getPosition();
            
            // Calculate distance between source and destination
            Point source = sourcePort.getPosition();
            Point destination = destinationPort.getPosition();
            
            // Calculate direction vector
            double totalDistance = Math.sqrt(
                Math.pow(destination.x - source.x, 2) + 
                Math.pow(destination.y - source.y, 2)
            );
            
            double dx = (destination.x - source.x) / totalDistance;
            double dy = (destination.y - source.y) / totalDistance;
            
            // Only offset the new packet if the last one is still near the start
            double distanceMoved = Math.sqrt(
                Math.pow(lastPosition.x - source.x, 2) + 
                Math.pow(lastPosition.y - source.y, 2)
            );
            
            // If the last packet is still close to the source (less than 25% down the wire)
            if (distanceMoved < totalDistance * 0.25) {
                // Offset this new packet slightly to avoid collision
                int offset = 20; // Pixels to offset
                int newX = source.x + (int)(dx * offset);
                int newY = source.y + (int)(dy * offset);
                packet.setPosition(new Point(newX, newY));
                System.out.println("Offsetting new packet to avoid collision: " + newX + "," + newY);
            }
        }
        
        packetsOnWire.add(packet);
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