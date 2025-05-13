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
        g.setColor(color);
        g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS));
        
        Point source = sourcePort.getPosition();
        Point destination = destinationPort.getPosition();
        
        g.drawLine(source.x, source.y, destination.x, destination.y);
        
        // Render packets on the wire
        for (Packet packet : packetsOnWire) {
            packet.render(g);
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
        packetsOnWire.add(packet);
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