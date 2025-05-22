package model;

import controller.Constants;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StandardSystem extends NetworkSystem {
    private static final int DEFAULT_STORAGE_CAPACITY = 5;
    private Random random;
    
    public StandardSystem(Point position, int width, int height) {
        super(position, width, height, DEFAULT_STORAGE_CAPACITY);
        this.random = new Random();
    }
    
    @Override
    public void render(Graphics2D g) {
        // Draw system body
        g.setColor(Constants.SYSTEM_COLOR);
        g.fillRect(position.x, position.y, width, height);
        
        // Draw system outline
        g.setColor(Color.BLACK);
        g.drawRect(position.x, position.y, width, height);
        
        // Draw active indicator
        int indicatorSize = 10;
        if (isActive) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.fillOval(position.x + width/2 - indicatorSize/2, 
                  position.y - indicatorSize - 5, 
                  indicatorSize, indicatorSize);
        
        // Render all ports
        for (Port port : inputPorts) {
            port.render(g);
        }
        
        for (Port port : outputPorts) {
            port.render(g);
        }
        
        // Render stored packets indicator
        int packetCount = storedPackets.size();
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(packetCount), position.x + width/2 - 5, position.y + height/2 + 5);
    }
    
    @Override
    public void update() {
        // Process stored packets if any
        if (!storedPackets.isEmpty() && !outputPorts.isEmpty()) {
            // Try to find an empty port first
            List<Port> emptyPorts = new ArrayList<>();
            
            for (Port outputPort : outputPorts) {
                // Check if any wire connected to this port has packets on it
                boolean isEmpty = true;
                
                for (Wire wire : Game.getInstance().getWires()) {
                    if (wire.getSourcePort() == outputPort && !wire.isEmpty()) {
                        isEmpty = false;
                        break;
                    }
                }
                
                if (isEmpty) {
                    emptyPorts.add(outputPort);
                }
            }
            
            if (!emptyPorts.isEmpty()) {
                // Choose port based on compatibility and availability
                Port selectedPort = null;
                Packet packetToSend = storedPackets.get(0);
                
                // First check for compatible empty ports
                for (Port port : emptyPorts) {
                    if (packetToSend.isCompatibleWithPort(port)) {
                        selectedPort = port;
                        break;
                    }
                }
                
                // If no compatible port found, pick a random empty port
                if (selectedPort == null && !emptyPorts.isEmpty()) {
                    selectedPort = emptyPorts.get(random.nextInt(emptyPorts.size()));
                }
                
                if (selectedPort != null) {
                    // Find wire connected to the selected port
                    for (Wire wire : Game.getInstance().getWires()) {
                        if (wire.getSourcePort() == selectedPort) {
                            // Remove packet from storage and send it through the wire
                            storedPackets.remove(packetToSend);
                            System.out.println("Sending packet from standard system through wire");
                            wire.addPacket(packetToSend);
                            break;
                        }
                    }
                }
            }
        }
        
        // System remains active as long as it has stored packets
        setActive(!storedPackets.isEmpty());
    }
} 