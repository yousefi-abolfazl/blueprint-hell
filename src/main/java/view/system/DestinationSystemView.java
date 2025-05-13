package view.system;

import static controller.Constants.DESTINATION_SYSTEM_COLOR;
import static controller.Constants.DESTINATION_SYSTEM_SIZE;
import static controller.Constants.PORT_SQUARE_SIZE;
import static controller.Constants.PORT_TRIANGLE_SIZE;
import static controller.Constants.PACKET_SQUARE_COLOR;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import view.packet.SquarePacketView;
import view.packet.TrianglePacketView;

public class DestinationSystemView extends SystemView {
    SquarePacketView squarePort1;
    SquarePacketView squarePort2;
    TrianglePacketView trianglePort;
    public DestinationSystemView(){
        this.size = DESTINATION_SYSTEM_SIZE;
        this.color = DESTINATION_SYSTEM_COLOR;
        squarePort1 = new SquarePacketView();
        squarePort2 = new SquarePacketView();
        trianglePort = new TrianglePacketView();
    }

    @Override
    public void render(int x, int y, Graphics2D g2d){
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(color);
        g2d.drawRoundRect(x, y, size, size, 10, 10);
        
    }


}