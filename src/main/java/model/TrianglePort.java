package model;

import controller.Constants;
import java.awt.*;

public class TrianglePort extends Port {
    
    public TrianglePort(Point position, boolean isInput, NetworkSystem parentSystem) {
        super(position, Constants.PORT_TRIANGLE_SIZE, isInput, parentSystem);
    }
    
    @Override
    public void render(Graphics2D g) {
        int portSize = size * 10;
        
        if (isInput) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }
        
        int[] xPoints = {
            position.x,
            position.x - portSize / 2,
            position.x + portSize / 2
        };
        
        int[] yPoints = {
            position.y - portSize / 2,
            position.y + portSize / 2,
            position.y + portSize / 2
        };
        
        g.fillPolygon(xPoints, yPoints, 3);
        
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
    }
} 