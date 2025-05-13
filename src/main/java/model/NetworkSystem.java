package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class NetworkSystem {
    protected Point position;
    protected int width;
    protected int height;
    protected List<Port> inputPorts;
    protected List<Port> outputPorts;
    protected List<Packet> storedPackets;
    protected boolean isActive;
    protected int storageCapacity;
    
    public NetworkSystem(Point position, int width, int height, int storageCapacity) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();
        this.storedPackets = new ArrayList<>();
        this.isActive = false;
        this.storageCapacity = storageCapacity;
    }
    
    public abstract void render(Graphics2D g);
    public abstract void update();
    
    public void addInputPort(Port port) {
        inputPorts.add(port);
    }
    
    public void addOutputPort(Port port) {
        outputPorts.add(port);
    }
    
    public boolean canStorePacket() {
        return storedPackets.size() < storageCapacity;
    }
    
    public void storePacket(Packet packet) {
        if (canStorePacket()) {
            storedPackets.add(packet);
        }
    }
    
    public List<Port> getInputPorts() {
        return inputPorts;
    }
    
    public List<Port> getOutputPorts() {
        return outputPorts;
    }
    
    public Point getPosition() {
        return position;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public List<Packet> getStoredPackets() {
        return storedPackets;
    }
    
    public int getStorageCapacity() {
        return storageCapacity;
    }
} 