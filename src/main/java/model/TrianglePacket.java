package model;

import controller.Constants;
import java.awt.*;

public class TrianglePacket extends Packet {
    
    public TrianglePacket(Point position) {
        super(position, Constants.PACKET_TRIANGLE_SIZE, 2); // Size 3, coin value 2
        this.maxSpeed = Constants.PACKET_TRIANGLE_SPEED;
        this.acceleration = 0.2;
        this.deceleration = 0.1;
    }
    
    @Override
    public void render(Graphics2D g) {
        int x = position.x;
        int y = position.y;
        int size = this.size * 10;
        
        int[] xPoints = {x, x - size/2, x + size/2};
        int[] yPoints = {y - size/2, y + size/2, y + size/2};

        // Draw outline
        g.setColor(Constants.PACKET_TRIANGLE_COLOR);
        g.setStroke(new BasicStroke(Constants.PACKET_TRIANGLE_THICKNESS));
        g.drawPolygon(xPoints, yPoints, 3);
        
        // Fill with semi-transparent color
        g.setColor(new Color(Constants.PACKET_TRIANGLE_COLOR.getRed(),
                             Constants.PACKET_TRIANGLE_COLOR.getGreen(),
                             Constants.PACKET_TRIANGLE_COLOR.getBlue(), 128));
        g.fillPolygon(xPoints, yPoints, 3);
        
        // Debug info: draw position coordinates
        System.out.println("Rendering triangle packet at: " + position.x + "," + position.y);
    }
    
    @Override
    public boolean isCompatibleWithPort(Port port) {
        return port instanceof TrianglePort;
    }
    
    @Override
    public void startMoving(Point target, Port sourcePort) {
        super.startMoving(target, sourcePort);
        
        // According to spec: Triangle packets have constant speed when moving from 
        // compatible ports, but have accelerating movement from incompatible ports
        if (sourcePort != null && !isCompatibleWithPort(sourcePort)) {
            // Enable acceleration for incompatible ports
            this.isAccelerating = true;
            this.acceleration = 0.3; // Higher acceleration for incompatible ports
        } else {
            // Constant speed for compatible ports
            this.currentSpeed = this.maxSpeed;
            this.isAccelerating = false;
        }
    }
    
    @Override
    public void startMoving(Point target) {
        super.startMoving(target);
        
        // Use the sourcePort from parent class
        Port srcPort = getSourcePort();
        if (srcPort != null && !isCompatibleWithPort(srcPort)) {
            // Enable acceleration for incompatible ports
            this.isAccelerating = true;
            this.acceleration = 0.3; // Higher acceleration for incompatible ports
        } else {
            // Constant speed for compatible ports
            this.currentSpeed = this.maxSpeed;
            this.isAccelerating = false;
        }
    }
} 