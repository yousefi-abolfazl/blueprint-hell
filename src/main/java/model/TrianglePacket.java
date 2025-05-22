package model;

import controller.Constants;
import java.awt.*;

public class TrianglePacket extends Packet {
    
    public TrianglePacket(Point position) {
        super(position, Constants.PACKET_TRIANGLE_SIZE, 2); // Size 3, coin value 2
        this.maxSpeed = Constants.PACKET_TRIANGLE_SPEED;
        this.currentSpeed = 0.8; // Start with some initial speed
        this.acceleration = 0.2;
        this.deceleration = 0.1;
    }
    
    @Override
    public void render(Graphics2D g) {
        // Calculate packet visual size (make it smaller)
        int visualSize = this.size * 6; // Reduced from 10 to 6
        
        // Calculate triangle points - now pointing to the right
        int[] xPoints = {position.x + visualSize/2, position.x - visualSize/2, position.x - visualSize/2};
        int[] yPoints = {position.y, position.y - visualSize/2, position.y + visualSize/2};

        // Draw main packet with solid fill, no glow effects
        g.setColor(Constants.PACKET_TRIANGLE_COLOR);
        g.fillPolygon(xPoints, yPoints, 3);
        
        // Draw outline 
        g.setStroke(new BasicStroke(Constants.PACKET_TRIANGLE_THICKNESS));
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
        
        // Draw noise indicator if noise > 0
        if (noise > 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.drawString(String.valueOf(noise), position.x - 3, position.y - 6);
        }
        
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