package model;

import controller.Constants;
import java.awt.*;

public class TrianglePacket extends Packet {
    
    public TrianglePacket(Point position) {
        super(position, Constants.PACKET_TRIANGLE_SIZE, 2); 
        this.maxSpeed = Constants.PACKET_TRIANGLE_SPEED;
        this.currentSpeed = 0.8; 
        this.acceleration = 0.2;
        this.deceleration = 0.1;
    }
    
    @Override
    public void render(Graphics2D g) {
        
        int visualSize = this.size * 6; 
        
        
        int[] xPoints;
        int[] yPoints;
        
        if (isMoving && targetPosition != null) {
            
            double dx = targetPosition.x - position.x;
            double dy = targetPosition.y - position.y;
            double angle = Math.atan2(dy, dx);
            
            
            xPoints = new int[]{
                (int)(position.x + Math.cos(angle) * visualSize/2),
                (int)(position.x + Math.cos(angle + Math.PI*2/3) * visualSize/2),
                (int)(position.x + Math.cos(angle + Math.PI*4/3) * visualSize/2)
            };
            
            yPoints = new int[]{
                (int)(position.y + Math.sin(angle) * visualSize/2),
                (int)(position.y + Math.sin(angle + Math.PI*2/3) * visualSize/2),
                (int)(position.y + Math.sin(angle + Math.PI*4/3) * visualSize/2)
            };
        } else {
            
            xPoints = new int[]{position.x + visualSize/2, position.x - visualSize/2, position.x - visualSize/2};
            yPoints = new int[]{position.y, position.y - visualSize/2, position.y + visualSize/2};
        }

        
        g.setColor(Constants.PACKET_TRIANGLE_COLOR);
        g.fillPolygon(xPoints, yPoints, 3);
        
        
        g.setStroke(new BasicStroke(Constants.PACKET_TRIANGLE_THICKNESS));
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
        
        
        if (noise > 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.drawString(String.valueOf(noise), position.x - 3, position.y - 6);
        }
        
        
        if (Constants.DEBUG_PACKET_MOVEMENT) {
            System.out.println("Rendering triangle packet at: " + position.x + "," + position.y);
        }
    }
    
    @Override
    public boolean isCompatibleWithPort(Port port) {
        return port instanceof TrianglePort;
    }
    
    @Override
    public void startMoving(Point target, Port sourcePort) {
        super.startMoving(target, sourcePort);
        
        
        if (sourcePort != null) {
            if (isCompatibleWithPort(sourcePort)) {
                
                this.currentSpeed = this.maxSpeed;
                this.isAccelerating = false;
            } else {
                
                this.currentSpeed = 0.5; 
                this.isAccelerating = true;
                this.acceleration = 0.35; 
            }
        } else {
            
            this.currentSpeed = 1.0;
            this.isAccelerating = true;
        }
        
        
        if (this.currentSpeed <= 0) {
            this.currentSpeed = 0.5;
        }
        
        System.out.println("Triangle packet starting to move with speed: " + this.currentSpeed + ", accelerating: " + this.isAccelerating);
    }
    
    @Override
    public void startMoving(Point target) {
        System.out.println("TrianglePacket startMoving called. Target: " + target);
        super.startMoving(target);
        
        
        Port srcPort = getSourcePort();
        if (srcPort != null) {
            if (isCompatibleWithPort(srcPort)) {
                
                this.currentSpeed = this.maxSpeed;
                this.isAccelerating = false;
            } else {
                
                this.currentSpeed = 0.5; 
                this.isAccelerating = true;
                this.acceleration = 0.35; 
            }
        } else {
            
            this.currentSpeed = 1.0;
            this.isAccelerating = true;
        }
        
        
        if (this.currentSpeed <= 0) {
            this.currentSpeed = 0.5;
        }
        
        System.out.println("Triangle packet starting to move (overloaded) with speed: " + this.currentSpeed + ", accelerating: " + this.isAccelerating);
    }
    
    // @Override
    // public void update() {
    //     System.out.println("Updating TrianglePacket. isMoving: " +
    //      isMoving + ", Position: " + getPosition());
    //     if (!isMoving) return;
        
        
    //     // if (targetPosition != null) {
            
    //     //     double distance = Math.sqrt(
    //     //         Math.pow(targetPosition.x - position.x, 2) +
    //     //         Math.pow(targetPosition.y - position.y, 2)
    //     //     );
            
            
    //     //     if (distance > 100 && currentSpeed < 1.0) {
    //     //         currentSpeed = Math.min(maxSpeed, currentSpeed + 0.1);
    //     //     }
    //     // }
        
        
    //     super.update();
        
        
    //     if (isMoving && currentSpeed < 0.5) {
    //         currentSpeed = 0.5;
    //     }
    // }
} 