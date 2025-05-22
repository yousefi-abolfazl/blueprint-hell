package model;

import controller.Constants;
import java.awt.*;
import java.util.Random;

public class SourceSystem extends NetworkSystem {
    private int packetGenerationFrequency;
    private int packetCounter;
    private boolean isSquarePacketGenerator;
    private Random random;
    private int localPacketsGenerated = 0; // تعداد پکت‌های تولید شده توسط این سیستم
    
    public SourceSystem(Point position, int width, int height, int packetGenerationFrequency, boolean isSquarePacketGenerator) {
        super(position, width, height, 0); // Source systems don't store packets
        this.packetGenerationFrequency = packetGenerationFrequency;
        this.packetCounter = 0;
        this.isSquarePacketGenerator = isSquarePacketGenerator;
        this.random = new Random();
        this.localPacketsGenerated = 0;
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
        System.out.println("SourceSystem.update() called - counter: " + packetCounter + ", frequency: " + packetGenerationFrequency);
        
        if (!isActive()) {
            System.out.println("SourceSystem is not active, skipping update");
            return;
        }
        
        packetCounter++;
        if (packetCounter >= packetGenerationFrequency) {
            System.out.println("Time to generate packet!");
            generatePacket();
            packetCounter = 0;
        }
    }
    
    private void generatePacket() {
        // Always ensure we're active when generating packets
        setActive(true);
        
        if (outputPorts.isEmpty()) {
            System.out.println("No output ports available in source system");
            return;
        }
        
        // Choose a random output port
        Port outputPort = outputPorts.get(random.nextInt(outputPorts.size()));
        Point packetPosition = new Point(outputPort.getPosition());
        
        System.out.println("Generating packet at " + packetPosition.x + "," + packetPosition.y);
        
        // Create the appropriate packet type
        Packet newPacket;
        if (isSquarePacketGenerator) {
            newPacket = new SquarePacket(packetPosition);
            System.out.println("Created a square packet with size " + newPacket.getSize());
        } else {
            newPacket = new TrianglePacket(packetPosition);
            System.out.println("Created a triangle packet with size " + newPacket.getSize());
        }
        
        // Set source port on the packet
        newPacket.setSourcePort(outputPort);
        
        // افزایش شمارنده‌های پکت
        localPacketsGenerated++;
        Game.getInstance().incrementTotalPacketsGenerated();
        System.out.println("Packet #" + localPacketsGenerated + " created by this source. Total packets in game: " + 
                          Game.getInstance().getTotalPacketsGenerated() + 
                          ", Lost packets: " + Game.getInstance().getTotalPacketsLost() + 
                          ", Current loss %: " + Game.getInstance().getPacketLoss());
        
        // Find connected wire and send packet through it
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
            // If a packet is created but cannot be sent, don't count it as lost
            Game.getInstance().decrementTotalPacketsGenerated();
        }
    }
    
    public void forceGeneratePacket() {
        // فراخوانی مستقیم متد generatePacket بدون در نظر گرفتن packet counter
        System.out.println("Forcing packet generation from source system");
        generatePacket();
    }
    
    public void setPacketGenerationFrequency(int frequency) {
        this.packetGenerationFrequency = frequency;
    }
} 