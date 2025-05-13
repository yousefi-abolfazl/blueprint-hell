package model;

import java.awt.*;

public abstract class Port {
    protected Point position;
    protected int size;
    protected boolean isInput;
    protected NetworkSystem parentSystem;
    
    public Port(Point position, int size, boolean isInput, NetworkSystem parentSystem) {
        this.position = position;
        this.size = size;
        this.isInput = isInput;
        this.parentSystem = parentSystem;
    }
    
    public abstract void render(Graphics2D g);
    
    public Point getPosition() {
        return position;
    }
    
    public void setPosition(Point position) {
        this.position = position;
    }
    
    public int getSize() {
        return size;
    }
    
    public boolean isInput() {
        return isInput;
    }
    
    public NetworkSystem getParentSystem() {
        return parentSystem;
    }
} 