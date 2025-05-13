package model;

import java.awt.*;

public abstract class Packet {
    protected Point position;
    protected int size;
    protected int noise;
    protected int speed;
    protected boolean isMoving;
    protected Point targetPosition;
    protected int coinValue;
    
    public Packet(Point position, int size, int coinValue) {
        this.position = position;
        this.size = size;
        this.noise = 0;
        this.isMoving = false;
        this.coinValue = coinValue;
    }
    
    public abstract void render(Graphics2D g);
    public abstract boolean isCompatibleWithPort(Port port);
    
    public Point getPosition() {
        return position;
    }
    
    public void setPosition(Point position) {
        this.position = position;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getNoise() {
        return noise;
    }
    
    public void addNoise(int amount) {
        this.noise += amount;
    }
    
    public void resetNoise() {
        this.noise = 0;
    }
    
    public boolean isLost() {
        return noise >= size;
    }
    
    public void startMoving(Point target) {
        this.isMoving = true;
        this.targetPosition = target;
    }
    
    public void stopMoving() {
        this.isMoving = false;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
    
    public int getCoinValue() {
        return coinValue;
    }
} 