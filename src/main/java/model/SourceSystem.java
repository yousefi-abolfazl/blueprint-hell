package model;

import controller.Constants;
import java.awt.*;
import java.util.Random;

public class SourceSystem extends NetworkSystem {
    private int packetGenerationFrequency;
    private int packetCounter;
    private boolean isSquarePacketGenerator;
    private Random random;
    
    public SourceSystem(Point position, int width, int height, int packetGenerationFrequency, boolean isSquarePacketGenerator) {
        super(position, width, height, 0); // Source systems don't store packets
        this.packetGenerationFrequency = packetGenerationFrequency;
        this.packetCounter = 0;
        this.isSquarePacketGenerator = isSquarePacketGenerator;
        this.random = new Random();
    }
    
    @Override
    public void render(Graphics2D g) {
        // Draw system body
        g.setColor(Constants.SOURCE_SYSTEM_COLOR);
        g.fillRect(position.x, position.y, width, height);
        
        // Draw system outline
        g.setColor(Color.BLACK);
        g.drawRect(position.x, position.y, width, height);
        
        // Draw source indicator
        g.setColor(Color.YELLOW);
        int indicatorSize = 15;
        g.fillOval(position.x + width/2 - indicatorSize/2, 
                  position.y - indicatorSize - 5, 
                  indicatorSize, indicatorSize);
        
        // Draw packet type indicator
        g.setColor(isSquarePacketGenerator ? Constants.PACKET_SQUARE_COLOR : Constants.PACKET_TRIANGLE_COLOR);
        if (isSquarePacketGenerator) {
            g.fillRect(position.x + width/2 - 10, position.y + height/2 - 10, 20, 20);
        } else {
            int[] xPoints = {position.x + width/2, position.x + width/2 - 10, position.x + width/2 + 10};
            int[] yPoints = {position.y + height/2 - 10, position.y + height/2 + 10, position.y + height/2 + 10};
            g.fillPolygon(xPoints, yPoints, 3);
        }
        
        // Render all ports
        for (Port port : outputPorts) {
            port.render(g);
        }
    }
    
    @Override
    public void update() {
        packetCounter++;
        if (packetCounter >= packetGenerationFrequency) {
            generatePacket();
            packetCounter = 0;
        }
    }
    
    private void generatePacket() {
        if (outputPorts.isEmpty()) {
            return;
        }
        
        // Choose a random output port
        Port outputPort = outputPorts.get(random.nextInt(outputPorts.size()));
        Point packetPosition = outputPort.getPosition();
        
        // Create the appropriate packet type
        Packet newPacket;
        if (isSquarePacketGenerator) {
            newPacket = new SquarePacket(packetPosition);
        } else {
            newPacket = new TrianglePacket(packetPosition);
        }
        
        // Logic to send the packet through the wire would be implemented here
        // For now, we just make sure the packet exists and is positioned correctly
    }
    
    public void setPacketGenerationFrequency(int frequency) {
        this.packetGenerationFrequency = frequency;
    }
} 