package model;

import controller.Constants;
import java.awt.*;

public class SquarePacket extends Packet {
    
    public SquarePacket(Point position) {
        super(position, Constants.PACKET_SQUARE_SIZE, 1); // Size 2, coin value 1
        this.speed = 5; // Default speed value
    }
    
    @Override
    public void render(Graphics2D g) {
        g.setColor(Constants.PACKET_SQUARE_COLOR);
        g.setStroke(new BasicStroke(Constants.PACKET_SQUARE_THICKNESS));
        int x = position.x - (size * 5);
        int y = position.y - (size * 5);
        int width = size * 10;
        g.drawRect(x, y, width, width);
    }
    
    @Override
    public boolean isCompatibleWithPort(Port port) {
        return port instanceof SquarePort;
    }
} 