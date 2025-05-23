package model;

import controller.Constants;
import java.awt.*;

public class DestinationSystem extends NetworkSystem {
    private int packetsReceived;
    
    public DestinationSystem(Point position, int width, int height) {
        super(position, width, height, 0); 
        this.packetsReceived = 0;
    }
    
    @Override
    public void render(Graphics2D g) {
        
        g.setColor(Constants.DESTINATION_SYSTEM_COLOR);
        g.fillRect(position.x, position.y, width, height);
        
        
        g.setColor(Color.BLACK);
        g.drawRect(position.x, position.y, width, height);
        
        
        g.setColor(Color.GREEN);
        int indicatorSize = 15;
        g.fillOval(position.x + width/2 - indicatorSize/2, 
                  position.y - indicatorSize - 5, 
                  indicatorSize, indicatorSize);
        
        
        g.setColor(Color.WHITE);
        g.drawString("Received: " + packetsReceived, position.x + 10, position.y + height/2);
        
        
        for (Port port : inputPorts) {
            port.render(g);
        }
    }
    
    @Override
    public void update() {
        
        
    }
    
    @Override
    public void receivePacket(Packet packet) {
        packetsReceived++;
        System.out.println("Destination system received packet #" + packetsReceived);
        
        
        Game.getInstance().incrementPacketsDelivered();
        
        
        Game.getInstance().addCoins(packet.getCoinValue());
        
        
        setActive(true);
        
        
        controller.SoundManager.getInstance().playSound("connection");
    }
    
    public int getPacketsReceived() {
        return packetsReceived;
    }
} 