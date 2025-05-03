package view.system;

import static controller.Constants.DESTINATION_SYSTEM_COLOR;
import static controller.Constants.DESTINATION_SYSTEM_SIZE;
import static controller.Constants.PORT_SQUARE_SIZE;
import static controller.Constants.PORT_TRIANGLE_SIZE;
import static controller.Constants.PACKET_SQUARE_COLOR;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class DestinationSystemView extends SystemView {
   int portSquareSize;
   int portTriSize;
    public DestinationSystemView(){
        this.size = DESTINATION_SYSTEM_SIZE;
        this.color = DESTINATION_SYSTEM_COLOR;
        this.portSquareSize = PORT_SQUARE_SIZE;
        this.portTriSize = PORT_TRIANGLE_SIZE;
    }

    @Override
    public void render(int x, int y, Graphics2D g2d){
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(color);
        g2d.drawRoundRect(x, y, size, size, 10, 10);
    }

    public void renderPort(int x, int y, Graphics2D g2d) {
        g2d.setColor(PACKET_SQUARE_COLOR);
    }

}