package view.system;


import java.awt.*;

abstract class SystemView {
    protected int size;
    protected Color color;

    abstract void render(int x, int y, Graphics2D g2d);
}


