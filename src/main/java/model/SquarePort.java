package model;

import controller.Constants;
import java.awt.*;

public class SquarePort extends Port {
    
    public SquarePort(Point position, boolean isInput, NetworkSystem parentSystem) {
        super(position, Constants.PORT_SQUARE_SIZE, isInput, parentSystem);
    }
    
    @Override
    public void render(Graphics2D g) {
        int portSize = size * 10;
        
        if (isInput) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }
        
        g.fillRect(position.x - portSize / 2, position.y - portSize / 2, portSize, portSize);
        
        g.setColor(Color.BLACK);
        g.drawRect(position.x - portSize / 2, position.y - portSize / 2, portSize, portSize);
    }
} 