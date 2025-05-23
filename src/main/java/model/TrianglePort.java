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
        
        
        Stroke originalStroke = g.getStroke();
        
        
        if (isInput) {
            g.setColor(Constants.PORT_INPUT_COLOR);
        } else {
            g.setColor(Constants.PORT_OUTPUT_COLOR);
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
        
        
        g.setStroke(new BasicStroke(1.0f));
        
        
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
        
        
        g.setStroke(originalStroke);
    }
} 