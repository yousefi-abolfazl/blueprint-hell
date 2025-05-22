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
            g.setColor(new Color(100, 200, 255, 180)); // آبی روشن با شفافیت
            g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS));
            g.drawLine(source.x, source.y, destination.x, destination.y);
        }
        
        // رندر معمولی وایر
        g.setColor(color);
        g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS));
        g.drawLine(source.x, source.y, destination.x, destination.y);
        
        // بازگرداندن وضعیت اصلی گرافیک
        g.setStroke(originalStroke);
        g.setColor(originalColor);
        
        // رندر همه پکت‌های روی وایر
        for (Packet packet : packetsOnWire) {
            packet.render(g);
        }
    }
    
    public void update() {
        // Update all packets on the wire using their physics-based movement
        for (Packet packet : new ArrayList<>(packetsOnWire)) {
            // First check if the packet is actually moving
            if (!packet.isMoving()) {
                // If the packet is not moving, start moving it toward the destination
                System.out.println("Starting packet movement on wire");
                packet.startMoving(destinationPort.getPosition(), sourcePort);
            }
            
            // Now update the packet's position based on physics
            packet.update();
            
            // Check if packet has reached destination
            double distanceToTarget = Math.sqrt(
                Math.pow(destinationPort.getPosition().x - packet.getPosition().x, 2) + 
                Math.pow(destinationPort.getPosition().y - packet.getPosition().y, 2)
            );
            
            if (distanceToTarget < 5 || !packet.isMoving()) {
                // Send the packet to the destination system
                System.out.println("Packet reached destination port");
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