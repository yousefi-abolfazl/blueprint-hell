package model;

import controller.Constants;
import java.awt.*;

public class DestinationSystem extends NetworkSystem {
    private int packetsReceived;
    
    public DestinationSystem(Point position, int width, int height) {
        super(position, width, height, 0); // Destination systems don't need to store packets
        this.packetsReceived = 0;
    }
    
    @Override
    public void render(Graphics2D g) {
        // Draw system body
        g.setColor(Constants.DESTINATION_SYSTEM_COLOR);
        g.fillRect(position.x, position.y, width, height);
        
        // Draw system outline
        g.setColor(Color.BLACK);
        g.drawRect(position.x, position.y, width, height);
        
        // Draw destination indicator
        g.setColor(Color.GREEN);
        int indicatorSize = 15;
        g.fillOval(position.x + width/2 - indicatorSize/2, 
                  position.y - indicatorSize - 5, 
                  indicatorSize, indicatorSize);
        
        // Draw received packets count
        g.setColor(Color.WHITE);
        g.drawString("Received: " + packetsReceived, position.x + 10, position.y + height/2);
        
        // Render all ports
        for (Port port : inputPorts) {
            port.render(g);
        }
    }
    
    @Override
    public void update() {
        // Destination systems receive packets but don't process them further
        // They just count the received packets
    }
    
    public void receivePacket(Packet packet) {
        packetsReceived++;
        // We could add more logic here, like checking if the packet is of the correct type
    }
    
    public int getPacketsReceived() {
        return packetsReceived;
    }
} 