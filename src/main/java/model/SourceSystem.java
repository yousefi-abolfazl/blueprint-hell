package model;

import controller.Constants;
import java.awt.*;
import java.util.Random;

public class SourceSystem extends NetworkSystem {
    private int packetGenerationFrequency;
    private int packetCounter;
    private boolean isSquarePacketGenerator;
    private Random random;
    private int localPacketsGenerated = 0; 
    
    public SourceSystem(Point position, int width, int height, int packetGenerationFrequency, boolean isSquarePacketGenerator) {
        super(position, width, height, 0); 
        this.packetGenerationFrequency = packetGenerationFrequency;
        this.packetCounter = 0;
        this.isSquarePacketGenerator = isSquarePacketGenerator;
        this.random = new Random();
        this.localPacketsGenerated = 0;
    }
    
    @Override
    public void render(Graphics2D g) {
        
        g.setColor(Constants.SOURCE_SYSTEM_COLOR);
        g.fillRect(position.x, position.y, width, height);
        
        
        g.setColor(Color.BLACK);
        g.drawRect(position.x, position.y, width, height);
        
        
        g.setColor(Color.YELLOW);
        int indicatorSize = 15;
        g.fillOval(position.x + width/2 - indicatorSize/2, 
                  position.y - indicatorSize - 5, 
                  indicatorSize, indicatorSize);
        
        
        g.setColor(isSquarePacketGenerator ? Constants.PACKET_SQUARE_COLOR : Constants.PACKET_TRIANGLE_COLOR);
        if (isSquarePacketGenerator) {
            g.fillRect(position.x + width/2 - 10, position.y + height/2 - 10, 20, 20);
        } else {
            int[] xPoints = {position.x + width/2, position.x + width/2 - 10, position.x + width/2 + 10};
            int[] yPoints = {position.y + height/2 - 10, position.y + height/2 + 10, position.y + height/2 + 10};
            g.fillPolygon(xPoints, yPoints, 3);
        }
        
        
        for (Port port : outputPorts) {
            port.render(g);
        }
    }
    
    @Override
    public void update() {
        if (!isActive()) return;
        
        packetCounter++;
        
        
        if (packetCounter >= packetGenerationFrequency) {
            generatePacket();
            packetCounter = 0;
        }
    }
    
    private void generatePacket() {
        
        setActive(true);
        
        if (outputPorts.isEmpty()) {
            System.out.println("No output ports available in source system");
            return;
        }
        
        
        Port outputPort = outputPorts.get(random.nextInt(outputPorts.size()));
        Point packetPosition = new Point(outputPort.getPosition());
        
        System.out.println("\n== GENERATING NEW PACKET ==");
        System.out.println("Position: " + packetPosition.x + "," + packetPosition.y);
        
        
        Packet newPacket;
        if (isSquarePacketGenerator) {
            newPacket = new SquarePacket(packetPosition);
            System.out.println("Created a square packet with size " + newPacket.getSize());
        } else {
            newPacket = new TrianglePacket(packetPosition);
            System.out.println("Created a triangle packet with size " + newPacket.getSize());
        }
        
        
        newPacket.setSourcePort(outputPort);
        
        
        localPacketsGenerated++;
        Game.getInstance().incrementTotalPacketsGenerated();
        
        
        boolean packetSent = false;
        for (Wire wire : Game.getInstance().getWires()) {
            if (wire.getSourcePort() == outputPort) {
                System.out.println("Found wire connected to output port. Adding packet to wire.");
                wire.addPacket(newPacket);
                packetSent = true;
                break;
            }
        }
        
        if (!packetSent) {
            System.err.println("WARNING: No wire connected to output port - packet not sent");
            
            Game.getInstance().decrementTotalPacketsGenerated();
        } else {
            
            Game.getInstance().forceUpdatePacketLoss();
        }
        
        System.out.println("================================");
    }
    
    public void randomizePacketCounter() {
        
        this.packetCounter = random.nextInt(packetGenerationFrequency);
        System.out.println("Randomized packet counter to " + packetCounter + " (frequency: " + packetGenerationFrequency + ")");
    }
    
    public void forceGeneratePacket() {
        
        System.out.println("Forcing packet generation from source system");
        generatePacket();
    }
    
    public void setPacketGenerationFrequency(int frequency) {
        this.packetGenerationFrequency = frequency;
    }
} 