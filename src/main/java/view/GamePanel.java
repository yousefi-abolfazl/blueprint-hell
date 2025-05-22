package view;

import controller.Constants;
import controller.SoundManager;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private static GamePanel INSTANCE;
    private Game game;
    private Port selectedPort;
    private Point wireEndPoint;
    private boolean isWiring = false;
    private boolean isGameRunning = false;
    
    private List<NetworkSystem> systems;
    private List<Wire> wires;
    
    // HUD components
    private JLabel wireLabel;
    private JLabel temporalLabel;
    private JLabel packetLossLabel;
    private JLabel coinsLabel;
    private JLabel instructionLabel;
    
    // UI flags for controls
    private boolean showHUD = true;
    private long lastHUDToggle = 0;
    
    public GamePanel() {
        setLayout(null);
        setFocusable(true);
        setBackground(new Color(15, 16, 22, 255));
        
        game = Game.getInstance();
        systems = new ArrayList<>();
        wires = new ArrayList<>();
        
        initializeHUD();
        initializeControls();
        
        // Add focus listener to help debug focus issues
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("GamePanel gained focus");
                repaint(); // Repaint when focus is gained
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("GamePanel lost focus");
            }
        });
        
        // Add key binding as an alternative to key listener
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        // Add Space key binding
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggleGameRunning");
        actionMap.put("toggleGameRunning", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Space pressed via key binding");
                toggleGameRunning();
                updateHUD();
                repaint();
            }
        });
        
        // Add Right arrow key binding
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "advanceTime");
        actionMap.put("advanceTime", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Right arrow pressed via key binding");
                if (!isGameRunning) {
                    game.advanceTime();
                    updateHUD();
                    repaint();
                }
            }
        });
        
        // Add Left arrow key binding
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "rewindTime");
        actionMap.put("rewindTime", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Left arrow pressed via key binding");
                if (!isGameRunning) {
                    game.rewindTime();
                    updateHUD();
                    repaint();
                }
            }
        });
        
        // Add S key binding for shop
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "showShop");
        actionMap.put("showShop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("S key pressed via key binding");
                showShop();
            }
        });
        
        // Add H key binding for HUD toggle
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "toggleHUD");
        actionMap.put("toggleHUD", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("H key pressed via key binding");
                toggleHUD();
                repaint();
            }
        });
        
        // Create a timer for regular updates
        Timer gameUpdateTimer = new Timer(16, e -> {
            if (isGameRunning && !game.isPaused()) {
                System.out.println("Game update timer tick at " + System.currentTimeMillis());
                
                // First update all systems for packet generation
                for (NetworkSystem system : systems) {
                    if (system instanceof SourceSystem) {
                        system.update();
                    }
                }
                
                // Then update all non-source systems
                for (NetworkSystem system : systems) {
                    if (!(system instanceof SourceSystem)) {
                        system.update();
                    }
                }
                
                // Then update all wires one by one to avoid simultaneous collisions
                for (Wire wire : wires) {
                    wire.update();
                    // Small yield to avoid wire updates happening in the same "moment"
                    try {
                        Thread.sleep(1); 
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // Finally update game state for collisions
                game.update();
                
                // Update UI
                updateHUD();
                repaint();
            }
        });
        gameUpdateTimer.start();
        
        // Force focus request on this panel
        SwingUtilities.invokeLater(this::forceFocus);
    }
    
    // Method to aggressively request focus
    public void forceFocus() {
        System.out.println("Forcing focus on GamePanel");
        requestFocusInWindow();
        requestFocus();
        
        // More aggressive focus request
        SwingUtilities.invokeLater(() -> {
            System.out.println("Requesting focus in invoke later");
            requestFocusInWindow();
            requestFocus();
            
            // Try to make the panel the focus owner
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .clearGlobalFocusOwner();
            requestFocusInWindow();
            
            // Try to make window active
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.toFront();
            }
        });
        
        // Create a recurring timer that checks for focus
        Timer focusTimer = new Timer(500, e -> {
            if (!hasFocus()) {
                System.out.println("GamePanel doesn't have focus - requesting again");
                requestFocusInWindow();
                requestFocus();
                
                // Try to make the panel the focus owner
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .clearGlobalFocusOwner();
                requestFocusInWindow();
            } else {
                System.out.println("GamePanel has focus!");
                ((Timer)e.getSource()).stop();
            }
        });
        focusTimer.setRepeats(true);
        focusTimer.start();
    }
    
    private void initializeHUD() {
        wireLabel = createHUDLabel("Wire: " + game.getRemainingWireLength());
        temporalLabel = createHUDLabel("Time: " + game.getTemporalProgress());
        packetLossLabel = createHUDLabel("Packet Loss: " + game.getPacketLoss() + "%");
        coinsLabel = createHUDLabel("Coins: " + game.getCoins());
        instructionLabel = createHUDLabel("[Space] Start/Pause  [→/←] Time  [H] Hide HUD  [S] Shop");
        
        wireLabel.setBounds(20, 20, 150, 30);
        temporalLabel.setBounds(20, 50, 150, 30);
        packetLossLabel.setBounds(20, 80, 150, 30);
        coinsLabel.setBounds(20, 110, 150, 30);
        
        // Position instruction label at the bottom of the screen
        instructionLabel.setBounds(20, getHeight() - 50, 600, 30);
        
        // Add shop button to the top right
        JButton shopButton = new JButton("Shop");
        shopButton.setBounds(getWidth() - 100, 20, 80, 30);
        shopButton.setBackground(new Color(255, 193, 7)); // Amber color
        shopButton.setForeground(Color.BLACK);
        shopButton.setFocusPainted(false);
        shopButton.setBorder(BorderFactory.createRaisedBevelBorder());
        shopButton.addActionListener(e -> showShop());
        shopButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        add(wireLabel);
        add(temporalLabel);
        add(packetLossLabel);
        add(coinsLabel);
        add(instructionLabel);
        add(shopButton);
        
        // Add component listener to reposition elements when panel resizes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                instructionLabel.setBounds(20, getHeight() - 50, 600, 30);
                shopButton.setBounds(getWidth() - 100, 20, 80, 30);
            }
        });
    }
    
    private JLabel createHUDLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }
    
    private void initializeControls() {
        // Mouse listener for port selection and wiring
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isGameRunning) return;
                
                // Check if clicked on a port
                Port clickedPort = findPortAt(e.getPoint());
                if (clickedPort != null && !clickedPort.isInput()) {
                    selectedPort = clickedPort;
                    isWiring = true;
                    wireEndPoint = e.getPoint();
                    System.out.println("Started wiring from output port at " + clickedPort.getPosition().x + "," + clickedPort.getPosition().y);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isGameRunning || !isWiring) return;
                
                isWiring = false;
                Port targetPort = findPortAt(e.getPoint());
                if (targetPort != null && targetPort.isInput() && 
                    selectedPort.getParentSystem() != targetPort.getParentSystem()) {
                    
                    System.out.println("Connecting to input port at " + targetPort.getPosition().x + "," + targetPort.getPosition().y);
                    Wire newWire = new Wire(selectedPort, targetPort);
                    int wireLength = newWire.getLength();
                    
                    if (wireLength <= game.getRemainingWireLength()) {
                        game.addWire(newWire);
                        wires.add(newWire);
                        updateHUD();
                        SoundManager.getInstance().playSound("connection");
                        System.out.println("Wire connected successfully! Length: " + wireLength);
                    } else {
                        System.out.println("Not enough wire remaining. Need: " + wireLength + ", Have: " + game.getRemainingWireLength());
                    }
                } else {
                    System.out.println("Invalid connection or no target port found");
                }
                
                selectedPort = null;
            }
        });
        
        // Mouse motion listener for updating wire end point
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isWiring) {
                    // بهبود پاسخگویی در زمان درگ کردن
                    SwingUtilities.invokeLater(() -> {
                        wireEndPoint = e.getPoint();
                        repaint(selectedPort.getPosition().x - 100, selectedPort.getPosition().y - 100, 
                                Math.abs(wireEndPoint.x - selectedPort.getPosition().x) + 200, 
                                Math.abs(wireEndPoint.y - selectedPort.getPosition().y) + 200);
                    });
                }
            }
        });
        
        // Key listener for game controls
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("Key pressed: " + KeyEvent.getKeyText(e.getKeyCode()) + " (code: " + e.getKeyCode() + ")");
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        System.out.println("Right arrow pressed - attempting to advance time");
                        if (!isGameRunning) game.advanceTime();
                        break;
                    case KeyEvent.VK_LEFT:
                        System.out.println("Left arrow pressed - attempting to rewind time");
                        if (!isGameRunning) game.rewindTime();
                        break;
                    case KeyEvent.VK_SPACE:
                        System.out.println("Space pressed - attempting to toggle game running");
                        toggleGameRunning();
                        break;
                    case KeyEvent.VK_H:
                        System.out.println("H pressed - attempting to toggle HUD");
                        toggleHUD();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.out.println("Escape pressed - checking game running status");
                        if (isGameRunning) {
                            isGameRunning = false;
                        }
                        break;
                    case KeyEvent.VK_S:
                        System.out.println("S pressed - attempting to show shop");
                        showShop();
                        break;
                }
                updateHUD();
                repaint();
            }
        });
    }
    
    private void showShop() {
        System.out.println("Opening shop...");
        
        // Store the current game running state before pausing
        boolean wasRunning = isGameRunning;
        if (isGameRunning) {
            // Temporarily pause the game while shop is open
            isGameRunning = false;
        }
        
        // Ensure the game is paused
        game.setPaused(true);
        
        // Create and configure the shop dialog
        ShopView shopView = new ShopView(GameFrame.getINSTANCE());
        
        // Add window listener to handle shop closing
        shopView.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                System.out.println("Shop closed, returning focus to GamePanel");
                
                // Request focus back
                requestFocusInWindow();
                forceFocus();
                
                // Restore previous game state
                if (wasRunning) {
                    isGameRunning = true;
                }
                
                // Unpause the game if it was running before
                game.setPaused(!wasRunning);
                
                // Update UI
                updateHUD();
                repaint();
            }
        });
        
        // Show the shop dialog
        shopView.setLocationRelativeTo(GameFrame.getINSTANCE());
        shopView.setVisible(true);
    }
    
    private Port findPortAt(Point point) {
        for (NetworkSystem system : systems) {
            // Check input ports
            for (Port port : system.getInputPorts()) {
                if (isPointNearPort(point, port)) {
                    return port;
                }
            }
            
            // Check output ports
            for (Port port : system.getOutputPorts()) {
                if (isPointNearPort(point, port)) {
                    return port;
                }
            }
        }
        return null;
    }
    
    private boolean isPointNearPort(Point point, Port port) {
        Point portPos = port.getPosition();
        int portSize = port.getSize() * 10;
        
        return point.x >= portPos.x - portSize / 2 &&
               point.x <= portPos.x + portSize / 2 &&
               point.y >= portPos.y - portSize / 2 &&
               point.y <= portPos.y + portSize / 2;
    }
    
    public void loadLevel(int level) {
        // Clear existing data
        systems.clear();
        wires.clear();
        game.resetGame();
        game.setCurrentLevel(level);
        
        // Load systems and ports for the level
        if (level == 1) {
            setupLevel1();
        } else if (level == 2) {
            setupLevel2();
        }
        
        isGameRunning = false;
        updateHUD();
        repaint();
    }
    
    private void setupLevel1() {
        // Example level 1 setup with a few systems
        int systemWidth = 100;
        int systemHeight = 80;
        
        // Source system generating square packets
        SourceSystem source1 = new SourceSystem(
            new Point(100, 200), 
            systemWidth, 
            systemHeight, 
            60, // Generate a packet every 60 frames (increased from 20)
            true // Generate square packets
        );
        
        // Add output ports to source
        SquarePort sourcePort1 = new SquarePort(
            new Point(100 + systemWidth, 220),
            false, 
            source1
        );
        source1.addOutputPort(sourcePort1);
        
        // Add the source to our lists
        systems.add(source1);
        game.addSystem(source1);
        
        // Regular system with square input and output
        StandardSystem system1 = new StandardSystem(
            new Point(300, 200),
            systemWidth,
            systemHeight
        );
        
        // Add ports to regular system
        SquarePort system1InputPort = new SquarePort(
            new Point(300, 220),
            true,
            system1
        );
        SquarePort system1OutputPort = new SquarePort(
            new Point(300 + systemWidth, 220),
            false,
            system1
        );
        
        system1.addInputPort(system1InputPort);
        system1.addOutputPort(system1OutputPort);
        
        // Add the system to our lists
        systems.add(system1);
        game.addSystem(system1);
        
        // Destination system for packets
        DestinationSystem dest1 = new DestinationSystem(
            new Point(500, 200),
            systemWidth,
            systemHeight
        );
        
        // Add input port to destination
        SquarePort destPort1 = new SquarePort(
            new Point(500, 220),
            true,
            dest1
        );
        dest1.addInputPort(destPort1);
        
        // Add the destination to our lists
        systems.add(dest1);
        game.addSystem(dest1);
    }
    
    private void setupLevel2() {
        // More complex level 2 with mixed packet types
        int systemWidth = 100;
        int systemHeight = 80;
        
        // Source system generating square packets
        SourceSystem squareSource = new SourceSystem(
            new Point(100, 150), 
            systemWidth, 
            systemHeight, 
            60, // Generate a packet every 60 frames (increased from 20)
            true // square packets
        );
        
        // Add output port to square source
        SquarePort squareSourcePort = new SquarePort(
            new Point(100 + systemWidth, 170),
            false, 
            squareSource
        );
        squareSource.addOutputPort(squareSourcePort);
        
        // Source system generating triangle packets
        SourceSystem triangleSource = new SourceSystem(
            new Point(100, 300), 
            systemWidth, 
            systemHeight, 
            80, // Generate a packet every 80 frames (increased from 30)
            false // triangle packets
        );
        
        // Add output port to triangle source
        TrianglePort triangleSourcePort = new TrianglePort(
            new Point(100 + systemWidth, 320),
            false, 
            triangleSource
        );
        triangleSource.addOutputPort(triangleSourcePort);
        
        // Add sources to lists
        systems.add(squareSource);
        systems.add(triangleSource);
        game.addSystem(squareSource);
        game.addSystem(triangleSource);
        
        // Intermediate systems
        StandardSystem system1 = new StandardSystem(
            new Point(300, 150),
            systemWidth,
            systemHeight
        );
        
        StandardSystem system2 = new StandardSystem(
            new Point(300, 300),
            systemWidth,
            systemHeight
        );
        
        // Add ports to system1
        SquarePort system1InputSquare = new SquarePort(
            new Point(300, 170),
            true,
            system1
        );
        TrianglePort system1InputTriangle = new TrianglePort(
            new Point(300, 190),
            true,
            system1
        );
        SquarePort system1Output = new SquarePort(
            new Point(300 + systemWidth, 170),
            false,
            system1
        );
        
        system1.addInputPort(system1InputSquare);
        system1.addInputPort(system1InputTriangle);
        system1.addOutputPort(system1Output);
        
        // Add ports to system2
        TrianglePort system2InputTriangle = new TrianglePort(
            new Point(300, 320),
            true,
            system2
        );
        SquarePort system2InputSquare = new SquarePort(
            new Point(300, 340),
            true,
            system2
        );
        TrianglePort system2Output = new TrianglePort(
            new Point(300 + systemWidth, 320),
            false,
            system2
        );
        
        system2.addInputPort(system2InputTriangle);
        system2.addInputPort(system2InputSquare);
        system2.addOutputPort(system2Output);
        
        // Add systems to lists
        systems.add(system1);
        systems.add(system2);
        game.addSystem(system1);
        game.addSystem(system2);
        
        // Destination systems
        DestinationSystem squareDest = new DestinationSystem(
            new Point(500, 150),
            systemWidth,
            systemHeight
        );
        
        DestinationSystem triangleDest = new DestinationSystem(
            new Point(500, 300),
            systemWidth,
            systemHeight
        );
        
        // Add input ports to destinations
        SquarePort squareDestInput = new SquarePort(
            new Point(500, 170),
            true,
            squareDest
        );
        squareDest.addInputPort(squareDestInput);
        
        TrianglePort triangleDestInput = new TrianglePort(
            new Point(500, 320),
            true,
            triangleDest
        );
        triangleDest.addInputPort(triangleDestInput);
        
        // Add destinations to lists
        systems.add(squareDest);
        systems.add(triangleDest);
        game.addSystem(squareDest);
        game.addSystem(triangleDest);
    }
    
    private void toggleGameRunning() {
        System.out.println("Toggle game running called. Current state: " + isGameRunning);
        isGameRunning = !isGameRunning;
        
        if (isGameRunning) {
            System.out.println("Game started running");
            
            // Make sure all source systems are active, but don't force generate packets immediately
            for (NetworkSystem system : systems) {
                if (system instanceof SourceSystem) {
                    System.out.println("Activating source system");
                    system.setActive(true);
                    
                    // Instead of forcing packet generation immediately, stagger them
                    SourceSystem sourceSystem = (SourceSystem) system;
                    // Reset counter to a random value to stagger packet generation
                    sourceSystem.randomizePacketCounter();
                }
            }
            
            SoundManager.getInstance().playSound("game_start");
            
            // Directly trigger an update
            System.out.println("Triggering immediate update");
            update();
        } else {
            System.out.println("Game paused");
            SoundManager.getInstance().playSound("game_pause");
        }
        
        // Update UI
        updateHUD();
        repaint();
    }
    
    private void toggleHUD() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHUDToggle > 250) { // Debounce toggle
            showHUD = !showHUD;
            wireLabel.setVisible(showHUD);
            temporalLabel.setVisible(showHUD);
            packetLossLabel.setVisible(showHUD);
            coinsLabel.setVisible(showHUD);
            instructionLabel.setVisible(showHUD);
            lastHUDToggle = currentTime;
        }
    }
    
    public void updateHUD() {
        wireLabel.setText("Wire: " + game.getRemainingWireLength());
        temporalLabel.setText("Time: " + game.getTemporalProgress());
        packetLossLabel.setText("Packet Loss: " + game.getPacketLoss() + "%");
        coinsLabel.setText("Coins: " + game.getCoins());
    }
    
    public void update() {
        if (isGameRunning && !game.isPaused()) {
            System.out.println("Updating game state...");
            
            // Display system status
            displayStatus();
            
            // Update all systems directly in addition to calling game.update()
            for (NetworkSystem system : systems) {
                System.out.println("Updating system: " + system.getClass().getSimpleName());
                if (system instanceof SourceSystem) {
                    // Ensure source systems are always active when game is running
                    system.setActive(true);
                    System.out.println("Ensuring source system is active");
                }
                system.update();
            }
            
            // Update all wires directly and log their packet counts
            for (Wire wire : wires) {
                int packetCount = wire.getPacketsOnWire().size();
                System.out.println("Updating wire with " + packetCount + " packets");
                wire.update();
            }
            
            // Also call the game's update method which handles collisions etc.
            game.update();
            
            if (game.isGameOver()) {
                isGameRunning = false;
                System.out.println("GAME OVER DETECTED! Packet Loss: " + game.getPacketLoss() + "%");
                showGameOverDialog();
            }
            
            updateHUD();
            repaint();
        }
    }
    
    private void displayStatus() {
        System.out.println("--- GAME STATUS ---");
        
        // Check systems
        System.out.println("Systems: " + systems.size());
        for (NetworkSystem system : systems) {
            System.out.println("  System: " + system.getClass().getSimpleName() + 
                              ", Active: " + system.isActive() +
                              ", InputPorts: " + system.getInputPorts().size() +
                              ", OutputPorts: " + system.getOutputPorts().size());
        }
        
        // Check wires
        System.out.println("Wires: " + wires.size());
        for (Wire wire : wires) {
            System.out.println("  Wire: Source=" + wire.getSourcePort().getClass().getSimpleName() + 
                              ", Destination=" + wire.getDestinationPort().getClass().getSimpleName() +
                              ", Packets=" + wire.getPacketsOnWire().size());
        }
        
        System.out.println("------------------");
    }
    
    private void showGameOverDialog() {
        SoundManager.getInstance().playSound("game_over");
        GameOverView gameOverView = new GameOverView(GameFrame.getINSTANCE());
        gameOverView.setVisible(true);
    }
    
    private void showLevelCompleteDialog() {
        SoundManager.getInstance().playSound("level_complete");
        LevelCompleteView levelCompleteView = new LevelCompleteView(GameFrame.getINSTANCE());
        levelCompleteView.setVisible(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw grid
        g2d.setColor(Color.DARK_GRAY);
        int cellSize = 50;
        int width = getWidth();
        int height = getHeight();
        int cols = width / cellSize;
        int rows = height / cellSize;
        
        for (int i = 0; i <= cols; i++) {
            int x = i * cellSize;
            g2d.drawLine(x, 0, x, height);
        }
        
        for (int i = 0; i <= rows; i++) {
            int y = i * cellSize;
            g2d.drawLine(0, y, width, y);
        }
        
        // Draw all wires
        for (Wire wire : wires) {
            wire.render(g2d);
        }
        
        // Draw all systems
        for (NetworkSystem system : systems) {
            system.render(g2d);
        }
        
        // Draw wire in progress
        if (isWiring && selectedPort != null) {
            Port hoverPort = findPortAt(wireEndPoint);
            boolean isValidConnection = false;
            
            // Check if we're hovering over a valid port for connection
            if (hoverPort != null && hoverPort.isInput() && 
                hoverPort.getParentSystem() != selectedPort.getParentSystem()) {
                
                // Calculate potential wire length
                int wireLength = (int) Math.sqrt(
                    Math.pow(hoverPort.getPosition().x - selectedPort.getPosition().x, 2) +
                    Math.pow(hoverPort.getPosition().y - selectedPort.getPosition().y, 2)
                );
                
                isValidConnection = wireLength <= game.getRemainingWireLength();
            }
            
            // Choose color based on connection validity
            g2d.setColor(isValidConnection ? 
                         Constants.WIRE_ALLOWABLE_COLOR : 
                         Constants.WIRE_UNALLOWABLE_COLOR);
            
            g2d.setStroke(new BasicStroke(Constants.WIRE_THICKNESS));
            Point sourcePos = selectedPort.getPosition();
            g2d.drawLine(sourcePos.x, sourcePos.y, wireEndPoint.x, wireEndPoint.y);
        }
        
        // If no wiring is in progress and game is not running, show help text
        if (!isWiring && !isGameRunning && systems.size() > 0) {
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String instructions = "Connect output ports (RED) to input ports (GREEN) by clicking and dragging";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(instructions);
            g2d.drawString(instructions, (width - textWidth) / 2, height - 100);
        }
    }
    
    public static GamePanel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GamePanel();
        }
        return INSTANCE;
    }
}