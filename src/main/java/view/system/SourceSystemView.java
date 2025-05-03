package view.system;

import static controller.Constants.SOURCE_SYSTEM_COLOR;
import static controller.Constants.SOURCE_SYSTEM_SIZE;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class SourceSystemView extends SystemView{

    public SourceSystemView() {
        this.size = SOURCE_SYSTEM_SIZE;
        this.color = SOURCE_SYSTEM_COLOR;
    }

    public void render(int x, int y, Graphics2D g2d){
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(color);
        g2d.drawRoundRect(x, y, size, size, 10, 10);
    }
}
