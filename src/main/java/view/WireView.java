package view;

import controller.Constants;
import model.Port;
import model.Wire;

import java.awt.*;

public class WireView {
    private Wire wire;
    private Color wireColor;
    private boolean isValid;
    
    public WireView(Wire wire) {
        this.wire = wire;
        this.wireColor = Constants.WIRE_ALLOWABLE_COLOR;
        this.isValid = true;
    }
    
    // For temporary wire display during placement
    public WireView(Port sourcePort, Point targetPoint, boolean isValid) {
        this.wire = null;
        this.isValid = isValid;
        this.wireColor = isValid ? Constants.WIRE_ALLOWABLE_COLOR : Constants.WIRE_UNALLOWABLE_COLOR;
    }
    
    public void render(Graphics2D g) {
        g.setColor(wireColor);
        g.setStroke(new BasicStroke(Constants.WIRE_THICKNESS));
        
        if (wire != null) {
            // Draw permanent wire
            Point sourcePos = wire.getSourcePort().getPosition();
            Point destPos = wire.getDestinationPort().getPosition();
            g.drawLine(sourcePos.x, sourcePos.y, destPos.x, destPos.y);
        }
    }
    
    public void setColor(Color color) {
        this.wireColor = color;
    }
    
    public void setValid(boolean valid) {
        this.isValid = valid;
        this.wireColor = valid ? Constants.WIRE_ALLOWABLE_COLOR : Constants.WIRE_UNALLOWABLE_COLOR;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public Wire getWire() {
        return wire;
    }
}
