package model;

import controller.Constants;
import java.awt.*;

public class SquarePacket extends Packet {
    
    public SquarePacket(Point position) {
        super(position, Constants.PACKET_SQUARE_SIZE, 1); // Size 2, coin value 1
        this.maxSpeed = Constants.PACKET_SQUARE_SPEED;
        this.acceleration = 0.2;
        this.deceleration = 0.1;
    }
    
    @Override
    public void render(Graphics2D g) {
        g.setColor(Constants.PACKET_SQUARE_COLOR);
        g.setStroke(new BasicStroke(Constants.PACKET_SQUARE_THICKNESS));
        int x = position.x - (size * 5);
        int y = position.y - (size * 5);
        int width = size * 10;
        
        // Draw outline
        g.drawRect(x, y, width, width);
        
        // Fill with semi-transparent color
        g.setColor(new Color(Constants.PACKET_SQUARE_COLOR.getRed(),
                             Constants.PACKET_SQUARE_COLOR.getGreen(),
                             Constants.PACKET_SQUARE_COLOR.getBlue(), 128));
        g.fillRect(x, y, width, width);
        
        // Debug info: draw position coordinates
        System.out.println("Rendering square packet at: " + position.x + "," + position.y);
    }
    
    @Override
    public boolean isCompatibleWithPort(Port port) {
        return port instanceof SquarePort;
    }
    
    @Override
    public void startMoving(Point target, Port sourcePort) {
        super.startMoving(target, sourcePort);
        
        // According to spec: Speed is half when moving from a compatible port
        if (sourcePort != null && isCompatibleWithPort(sourcePort)) {
            this.maxSpeed = Constants.PACKET_SQUARE_SPEED / 2.0;
        } else {
            this.maxSpeed = Constants.PACKET_SQUARE_SPEED;
        }
    }
    
    @Override
    public void startMoving(Point target) {
        super.startMoving(target);
        
        // Set speed based on sourcePort (which might be null)
        if (getSourcePort() != null && isCompatibleWithPort(getSourcePort())) {
            this.maxSpeed = Constants.PACKET_SQUARE_SPEED / 2.0;
        } else {
            this.maxSpeed = Constants.PACKET_SQUARE_SPEED;
        }
    }
    

} 