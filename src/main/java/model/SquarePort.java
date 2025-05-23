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
        
        
        Stroke originalStroke = g.getStroke();
        
        
        if (isInput) {
            g.setColor(Constants.PORT_INPUT_COLOR);
        } else {
            g.setColor(Constants.PORT_OUTPUT_COLOR);
        }
        
        
        g.fillRect(position.x - portSize / 2, position.y - portSize / 2, portSize, portSize);
        
        
        g.setStroke(new BasicStroke(1.0f));
        
        
        g.setColor(Color.BLACK);
        g.drawRect(position.x - portSize / 2, position.y - portSize / 2, portSize, portSize);
        
        
        g.setStroke(originalStroke);
    }
} 