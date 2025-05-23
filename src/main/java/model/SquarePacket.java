package model;

import controller.Constants;
import java.awt.*;

public class SquarePacket extends Packet {
    
    public SquarePacket(Point position) {
        super(position, Constants.PACKET_SQUARE_SIZE, 1); 
        this.maxSpeed = Constants.PACKET_SQUARE_SPEED;
        this.currentSpeed = 1.0; 
        this.acceleration = 0.2;
        this.deceleration = 0.1;
    }
    
    @Override
    public void render(Graphics2D g) {
        
        int visualSize = size * 6; 
        int x = position.x - (visualSize / 2);
        int y = position.y - (visualSize / 2);
        
        
        g.setColor(Constants.PACKET_SQUARE_COLOR);
        g.fillRect(x, y, visualSize, visualSize);
        
        
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(Constants.PACKET_SQUARE_THICKNESS));
        g.drawRect(x, y, visualSize, visualSize);
        
        
        if (noise > 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.drawString(String.valueOf(noise), position.x - 3, position.y - 6);
        }
        
        
        System.out.println("Rendering square packet at: " + position.x + "," + position.y);
    }
    
    @Override
    public boolean isCompatibleWithPort(Port port) {
        return port instanceof SquarePort;
    }
    
    @Override
    public void startMoving(Point target, Port sourcePort) {
        super.startMoving(target, sourcePort);
        
        
        if (sourcePort != null && isCompatibleWithPort(sourcePort)) {
            this.maxSpeed = Constants.PACKET_SQUARE_SPEED / 2.0;
        } else {
            this.maxSpeed = Constants.PACKET_SQUARE_SPEED;
        }
    }
    
    @Override
    public void startMoving(Point target) {
        super.startMoving(target);
        
        
        if (getSourcePort() != null && isCompatibleWithPort(getSourcePort())) {
            this.maxSpeed = Constants.PACKET_SQUARE_SPEED / 2.0;
        } else {
            this.maxSpeed = Constants.PACKET_SQUARE_SPEED;
        }
    }
    

} 