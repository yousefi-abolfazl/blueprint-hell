package view.packet;

import java.awt.*;

abstract class PacketView {
    protected int size;
    protected Color color;
    protected int thickness;

    public abstract void render(Graphics g, int x, int y);
}