package view.system;

import static controller.Constants.SOURCE_SYSTEM_COLOR;
import static controller.Constants.SOURCE_SYSTEM_SIZE;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.*;
import view.packet.SquarePacketView;
import view.packet.TrianglePacketView;

public class SourceSystemView extends SystemView{
    SquarePacketView squarePort;
    TrianglePacketView trianglePort;
    public SourceSystemView() {
        this.size = SOURCE_SYSTEM_SIZE;
        this.color = SOURCE_SYSTEM_COLOR;
        trianglePort = new TrianglePacketView();
        squarePort = new SquarePacketView();
    }

    public void render(int x, int y, Graphics2D g2d){
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(color);
        g2d.drawRoundRect(x, y, size, size, 10, 10);
        int gap = 3;
        int xPort = x + size/2;
        int yPort = y - size/2 + gap;
        squarePort.render(g2d, xPort, yPort);
        trianglePort.render(g2d, xPort, yPort + gap);
    }

    public Point getSquarePortLocation(){
        return squarePort.getLocation();
    }

    public Point getTrianglePortLocation(){
        return trianglePort.getLocation();
    }
}
