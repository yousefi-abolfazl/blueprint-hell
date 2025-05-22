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
    
    // Movement physics
    protected double currentSpeed;
    protected double maxSpeed;
    protected double acceleration;
    protected double deceleration;
    protected double velocityX;
    protected double velocityY;
    protected boolean isAccelerating;
    protected Port sourcePort;
    
    public Packet(Point position, int size, int coinValue) {
        this.position = position;
        this.size = size;
        this.noise = 0;
        this.isMoving = false;
        this.coinValue = coinValue;
        this.sourcePort = null;
        
        // Initialize physics values
        this.maxSpeed = 5.0;
        this.currentSpeed = 0.0;
        this.acceleration = 0.2;
        this.deceleration = 0.1;
        this.velocityX = 0;
        this.velocityY = 0;
        this.isAccelerating = false;
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
        // یک پکت زمانی از دست رفته محسوب می‌شود که noise آن برابر یا بیشتر از size باشد
        boolean lost = noise >= size;
        if (lost) {
            System.out.println("Packet LOST! Noise (" + noise + ") >= Size (" + size + ")");
        }
        return lost;
    }
    
    public void startMoving(Point target, Port sourcePort) {
        this.isMoving = true;
        this.targetPosition = target;
        this.isAccelerating = true;
        this.sourcePort = sourcePort;
        
        // Calculate direction vector
        double dx = target.x - position.x;
        double dy = target.y - position.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        // Normalize and set initial velocity
        if (length > 0) {
            this.velocityX = (dx / length);
            this.velocityY = (dy / length);
        }
    }
    
    // Overload for backward compatibility
    public void startMoving(Point target) {
        startMoving(target, null);
    }
    
    public void update() {
        if (!isMoving) return;
        
        // Update speed based on acceleration/deceleration
        if (isAccelerating) {
            currentSpeed = Math.min(maxSpeed, currentSpeed + acceleration);
        } else {
            currentSpeed = Math.max(0, currentSpeed - deceleration);
        }
        
        // Move packet based on velocity and current speed
        double newX = position.x + (velocityX * currentSpeed);
        double newY = position.y + (velocityY * currentSpeed);
        position = new Point((int)newX, (int)newY);
        
        // Check if we're close to target
        double distanceToTarget = Math.sqrt(
            Math.pow(targetPosition.x - position.x, 2) + 
            Math.pow(targetPosition.y - position.y, 2)
        );
        
        // Start decelerating when we're getting close to the target
        if (distanceToTarget < 50) {
            isAccelerating = false;
        }
        
        // Stop when we reach target or have stopped
        if (distanceToTarget < 5 || currentSpeed <= 0) {
            position = targetPosition;
            stopMoving();
        }
    }
    
    public void stopMoving() {
        this.isMoving = false;
        this.currentSpeed = 0;
        this.velocityX = 0;
        this.velocityY = 0;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
    
    public void setSpeed(double speed) {
        this.maxSpeed = speed;
    }
    
    public double getCurrentSpeed() {
        return currentSpeed;
    }
    
    public int getCoinValue() {
        return coinValue;
    }
    
    public Port getSourcePort() {
        return sourcePort;
    }
    
    public void setSourcePort(Port sourcePort) {
        this.sourcePort = sourcePort;
    }
} 