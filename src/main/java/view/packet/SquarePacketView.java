package view.packet;
import java.awt.geom.GeneralPath;
import static controller.Constants.PACKET_SQUARE_SIZE;
import static controller.Constants.PACKET_SQUARE_COLOR;
import static controller.Constants.PACKET_SQUARE_THICKNESS;
import java.awt.*;

public class SquarePacketView extends PacketView {
    private Point location;
    public SquarePacketView() {
        this.size = PACKET_SQUARE_SIZE;
        this.color = PACKET_SQUARE_COLOR;
        this.thickness = PACKET_SQUARE_THICKNESS;
    }

    @Override
    public void render(Graphics g, int x, int y) {
        location = new Point(x, y);
        Graphics2D g2d = (Graphics2D) g;

        int[] xPoints = { x - size / 2, x + size / 2, x + size / 2, x - size / 2 };
        int[] yPoints = { y - size / 2, y - size / 2, y + size / 2, y + size / 2 };

        GeneralPath path = new GeneralPath();

        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                path.moveTo(xPoints[i], yPoints[i]);
                continue;
            }
            path.lineTo(xPoints[i], yPoints[i]);
        }
        path.closePath();
        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g2d.setColor(color);
        g2d.draw(path);
    }

    public Point getLocation(){
        if (location == null){
            return new Point(0, 0);
        }
        return location;
    }
}