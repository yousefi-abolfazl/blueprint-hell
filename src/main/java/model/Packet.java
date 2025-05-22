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
            System.out.println("******************************************");
            System.out.println("PACKET LOSS CHECK: Noise(" + noise + ") >= Size(" + size + ")");
            System.out.println("Packet lost at position: " + position.x + "," + position.y);
            System.out.println("Packet was moving: " + isMoving);
            if (isMoving && targetPosition != null) {
                System.out.println("Moving toward: " + targetPosition.x + "," + targetPosition.y);
                System.out.println("Current speed: " + currentSpeed);
            }
            System.out.println("******************************************");
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
        
        // اطمینان از اینکه targetPosition تنظیم شده است
        if (targetPosition == null) {
            System.out.println("ERROR: Packet is moving but target position is null!");
            stopMoving();
            return;
        }
        
        // Update speed based on acceleration/deceleration
        if (isAccelerating) {
            currentSpeed = Math.min(maxSpeed, currentSpeed + acceleration);
        } else {
            currentSpeed = Math.max(0, currentSpeed - deceleration);
        }
        
        // اگر سرعت بسیار کم است، یک حداقل سرعت اعمال کنیم تا پکت گیر نکند
        if (currentSpeed < 0.5) {
            currentSpeed = 0.5;
            System.out.println("Increasing packet speed to minimum threshold");
        }
        
        // Calculate actual distance to move this frame
        double moveDistance = currentSpeed;
        
        // Calculate direction vector
        double dx = targetPosition.x - position.x;
        double dy = targetPosition.y - position.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Check if we've reached the destination
        if (distance <= moveDistance) {
            // We've arrived at the destination
            position = new Point(targetPosition);
            stopMoving();
            System.out.println("Packet arrived at destination");
            return;
        }
        
        // Normalize direction vector
        dx = (dx / distance);
        dy = (dy / distance);
        
        // Calculate new position
        int newX = position.x + (int)(dx * moveDistance);
        int newY = position.y + (int)(dy * moveDistance);
        
        // بررسی آیا پکت واقعاً حرکت کرد
        if (newX == position.x && newY == position.y) {
            // اگر پکت حرکت نکرد (به خاطر تبدیل به عدد صحیح)، اندکی سرعت را افزایش دهیم
            currentSpeed += 0.5;
            moveDistance = currentSpeed;
            newX = position.x + (int)(dx * moveDistance);
            newY = position.y + (int)(dy * moveDistance);
            System.out.println("Packet was stuck, increasing speed to: " + currentSpeed);
        }
        
        // Set new position
        position = new Point(newX, newY);
        
        // Debug output
        System.out.println("Packet moved to: " + position.x + "," + position.y + 
                          " (speed=" + currentSpeed + ", distance to target=" + 
                          Math.sqrt(Math.pow(targetPosition.x - position.x, 2) + 
                                   Math.pow(targetPosition.y - position.y, 2)) + ")");
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