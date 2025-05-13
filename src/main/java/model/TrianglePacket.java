package model;

import controller.Constants;
import java.awt.*;

public class TrianglePacket extends Packet {
    
    public TrianglePacket(Point position) {
        super(position, Constants.PACKET_TRIANGLE_SIZE, 2); // Size 3, coin value 2
        this.speed = 3; // Default speed value, slower than square packet
    }
    
    @Override
    public void render(Graphics2D g) {
        g.setColor(Constants.PACKET_TRIANGLE_COLOR);
        g.setStroke(new BasicStroke(Constants.PACKET_TRIANGLE_THICKNESS));
        
        int size = this.size * 10;
        int[] xPoints = {
            position.x,
            position.x - size/2,
            position.x + size/2
        };
        
        int[] yPoints = {
            position.y - size/2,
            position.y + size/2,
            position.y + size/2
        };
        
        g.drawPolygon(xPoints, yPoints, 3);
    }
    
    @Override
    public boolean isCompatibleWithPort(Port port) {
        return port instanceof TrianglePort;
    }
} 