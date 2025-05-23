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
    
    
    private JLabel wireLabel;
    private JLabel temporalLabel;
    private JLabel packetLossLabel;
    private JLabel deliveredLabel;
    private JLabel coinsLabel;
    private JLabel instructionLabel;
    
    
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
        
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("GamePanel gained focus");
                repaint(); 
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("GamePanel lost focus");
            }
        });
        
        
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        
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
        
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "showShop");
        actionMap.put("showShop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("S key pressed via key binding");
                showShop();
            }
        });
        
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "toggleHUD");
        actionMap.put("toggleHUD", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("H key pressed via key binding");
                toggleHUD();
                repaint();
            }
        });
        
        
        Timer gameUpdateTimer = new Timer(16, e -> {
            if (isGameRunning && !game.isPaused()) {
                System.out.println("Game update timer tick at " + System.currentTimeMillis());
                
                
                for (NetworkSystem system : systems) {
                    if (system instanceof SourceSystem) {
                        system.update();
                    }
                }
                
                
                for (NetworkSystem system : systems) {
                    if (!(system instanceof SourceSystem)) {
                        system.update();
                    }
                }
                
                
                for (Wire wire : wires) {
                    wire.update();
                    
                    try {
                        Thread.sleep(1); 
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                
                game.update();
                
                
                updateHUD();
                repaint();
            }
        });
        gameUpdateTimer.start();
        
        
        SwingUtilities.invokeLater(this::forceFocus);
    }
    
    
    public void forceFocus() {
        System.out.println("Forcing focus on GamePanel");
        requestFocusInWindow();
        requestFocus();
        
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("Requesting focus in invoke later");
            requestFocusInWindow();
            requestFocus();
            
            
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .clearGlobalFocusOwner();
            requestFocusInWindow();
            
            
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.toFront();
            }
        });
        
        
        Timer focusTimer = new Timer(500, e -> {
            if (!hasFocus()) {
                System.out.println("GamePanel doesn't have focus - requesting again");
                requestFocusInWindow();
                requestFocus();
                
                
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
        deliveredLabel = createHUDLabel("Delivered: " + game.getTotalPacketsDelivered());
        coinsLabel = createHUDLabel("Coins: " + game.getCoins());
        instructionLabel = createHUDLabel("[Space] Start/Pause  [→/←] Time  [H] Hide HUD  [S] Shop");
        
        wireLabel.setBounds(20, 20, 150, 30);
        temporalLabel.setBounds(20, 50, 150, 30);
        packetLossLabel.setBounds(20, 80, 200, 30);
        deliveredLabel.setBounds(20, 110, 150, 30);
        coinsLabel.setBounds(20, 140, 150, 30);
        
        
        instructionLabel.setBounds(20, getHeight() - 50, 600, 30);
        
        
        JButton shopButton = new JButton("Shop");
        shopButton.setBounds(getWidth() - 100, 20, 80, 30);
        shopButton.setBackground(new Color(255, 193, 7)); 
        shopButton.setForeground(Color.BLACK);
        shopButton.setFocusPainted(false);
        shopButton.setBorder(BorderFactory.createRaisedBevelBorder());
        shopButton.addActionListener(e -> showShop());
        shopButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        add(wireLabel);
        add(temporalLabel);
        add(packetLossLabel);
        add(deliveredLabel);
        add(coinsLabel);
        add(instructionLabel);
        add(shopButton);
        
        
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
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isGameRunning) return;
                
                
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
        
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isWiring) {
                    
                    SwingUtilities.invokeLater(() -> {
                        wireEndPoint = e.getPoint();
                        repaint(selectedPort.getPosition().x - 100, selectedPort.getPosition().y - 100, 
                                Math.abs(wireEndPoint.x - selectedPort.getPosition().x) + 200, 
                                Math.abs(wireEndPoint.y - selectedPort.getPosition().y) + 200);
                    });
                }
            }
        });
        
        
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
        
        
        boolean wasRunning = isGameRunning;
        if (isGameRunning) {
            
            isGameRunning = false;
        }
        
        
        game.setPaused(true);
        
        
        ShopView shopView = new ShopView(GameFrame.getINSTANCE());
        
        
        shopView.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                System.out.println("Shop closed, returning focus to GamePanel");
                
                
                requestFocusInWindow();
                forceFocus();
                
                
                if (wasRunning) {
                    isGameRunning = true;
                }
                
                
                game.setPaused(!wasRunning);
                
                
                updateHUD();
                repaint();
            }
        });
        
        
        shopView.setLocationRelativeTo(GameFrame.getINSTANCE());
        shopView.setVisible(true);
    }
    
    private Port findPortAt(Point point) {
        for (NetworkSystem system : systems) {
            
            for (Port port : system.getInputPorts()) {
                if (isPointNearPort(point, port)) {
                    return port;
                }
            }
            
            
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
        
        systems.clear();
        wires.clear();
        game.resetGame();
        game.setCurrentLevel(level);
        
        
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
        
        int systemWidth = 100;
        int systemHeight = 80;
        
        
        SourceSystem source1 = new SourceSystem(
            new Point(100, 200), 
            systemWidth, 
            systemHeight, 
            60, 
            true 
        );
        
        
        SquarePort sourcePort1 = new SquarePort(
            new Point(100 + systemWidth, 220),
            false, 
            source1
        );
        source1.addOutputPort(sourcePort1);
        
        
        systems.add(source1);
        game.addSystem(source1);
        
        
        StandardSystem system1 = new StandardSystem(
            new Point(300, 200),
            systemWidth,
            systemHeight
        );
        
        
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
        
        
        systems.add(system1);
        game.addSystem(system1);
        
        
        DestinationSystem dest1 = new DestinationSystem(
            new Point(500, 200),
            systemWidth,
            systemHeight
        );
        
        
        SquarePort destPort1 = new SquarePort(
            new Point(500, 220),
            true,
            dest1
        );
        dest1.addInputPort(destPort1);
        
        
        systems.add(dest1);
        game.addSystem(dest1);
    }
    
    private void setupLevel2() {
        
        int systemWidth = 100;
        int systemHeight = 80;
        
        
        SourceSystem squareSource = new SourceSystem(
            new Point(100, 150), 
            systemWidth, 
            systemHeight, 
            60, 
            true 
        );
        
        
        SquarePort squareSourcePort = new SquarePort(
            new Point(100 + systemWidth, 170),
            false, 
            squareSource
        );
        squareSource.addOutputPort(squareSourcePort);
        
        
        SourceSystem triangleSource = new SourceSystem(
            new Point(100, 300), 
            systemWidth, 
            systemHeight, 
            80, 
            false 
        );
        
        
        TrianglePort triangleSourcePort = new TrianglePort(
            new Point(100 + systemWidth, 320),
            false, 
            triangleSource
        );
        triangleSource.addOutputPort(triangleSourcePort);
        
        
        systems.add(squareSource);
        systems.add(triangleSource);
        game.addSystem(squareSource);
        game.addSystem(triangleSource);
        
        
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
        
        
        systems.add(system1);
        systems.add(system2);
        game.addSystem(system1);
        game.addSystem(system2);
        
        
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
            
            
            for (NetworkSystem system : systems) {
                if (system instanceof SourceSystem) {
                    System.out.println("Activating source system");
                    system.setActive(true);
                    
                    
                    SourceSystem sourceSystem = (SourceSystem) system;
                    
                    sourceSystem.randomizePacketCounter();
                }
            }
            
            SoundManager.getInstance().playSound("game_start");
            
            
            System.out.println("Triggering immediate update");
            update();
        } else {
            System.out.println("Game paused");
            SoundManager.getInstance().playSound("game_pause");
        }
        
        
        updateHUD();
        repaint();
    }
    
    private void toggleHUD() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHUDToggle > 250) { 
            showHUD = !showHUD;
            wireLabel.setVisible(showHUD);
            temporalLabel.setVisible(showHUD);
            packetLossLabel.setVisible(showHUD);
            deliveredLabel.setVisible(showHUD);
            coinsLabel.setVisible(showHUD);
            instructionLabel.setVisible(showHUD);
            lastHUDToggle = currentTime;
        }
    }
    
    public void updateHUD() {
        wireLabel.setText("Wire: " + game.getRemainingWireLength());
        temporalLabel.setText("Time: " + game.getTemporalProgress());
        
        
        packetLossLabel.setText(String.format("Packet Loss: %d%% (%d lost / %d delivered / %d total)",
            game.getPacketLoss(),
            game.getTotalPacketsLost(),
            game.getTotalPacketsDelivered(),
            game.getTotalPacketsGenerated()));
        
        deliveredLabel.setText(String.format("Delivered: %d (In transit: %d)", 
            game.getTotalPacketsDelivered(),
            game.getTotalPacketsGenerated() - game.getTotalPacketsDelivered()));
        
        coinsLabel.setText("Coins: " + game.getCoins());
    }
    
    public void update() {
        if (isGameRunning && !game.isPaused()) {
            System.out.println("Updating game state...");
            
            
            displayStatus();
            
            
            for (NetworkSystem system : systems) {
                System.out.println("Updating system: " + system.getClass().getSimpleName());
                if (system instanceof SourceSystem) {
                    
                    system.setActive(true);
                    System.out.println("Ensuring source system is active");
                }
                system.update();
            }
            
            
            for (Wire wire : wires) {
                int packetCount = wire.getPacketsOnWire().size();
                System.out.println("Updating wire with " + packetCount + " packets");
                wire.update();
            }
            
            
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
        
        
        System.out.println("Systems: " + systems.size());
        for (NetworkSystem system : systems) {
            System.out.println("  System: " + system.getClass().getSimpleName() + 
                              ", Active: " + system.isActive() +
                              ", InputPorts: " + system.getInputPorts().size() +
                              ", OutputPorts: " + system.getOutputPorts().size());
            
            if (system instanceof DestinationSystem) {
                System.out.println("    Packets received: " + ((DestinationSystem) system).getPacketsReceived());
            }
        }
        
        
        System.out.println("Wires: " + wires.size());
        for (Wire wire : wires) {
            System.out.println("  Wire: Source=" + wire.getSourcePort().getClass().getSimpleName() + 
                              ", Destination=" + wire.getDestinationPort().getClass().getSimpleName() +
                              ", Packets=" + wire.getPacketsOnWire().size());
        }
        
        
        System.out.println("Packet statistics:");
        System.out.println("  Generated: " + game.getTotalPacketsGenerated());
        System.out.println("  Delivered: " + game.getTotalPacketsDelivered());
        System.out.println("  Lost: " + game.getTotalPacketsLost());
        System.out.println("  In transit: " + (game.getTotalPacketsGenerated() - game.getTotalPacketsDelivered() - game.getTotalPacketsLost()));
        System.out.println("  Loss rate: " + game.getPacketLoss() + "%");
        
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
        
        
        for (Wire wire : wires) {
            wire.render(g2d);
        }
        
        
        for (NetworkSystem system : systems) {
            system.render(g2d);
        }
        
        
        if (isWiring && selectedPort != null) {
            Port hoverPort = findPortAt(wireEndPoint);
            boolean isValidConnection = false;
            
            
            if (hoverPort != null && hoverPort.isInput() && 
                hoverPort.getParentSystem() != selectedPort.getParentSystem()) {
                
                
                int wireLength = (int) Math.sqrt(
                    Math.pow(hoverPort.getPosition().x - selectedPort.getPosition().x, 2) +
                    Math.pow(hoverPort.getPosition().y - selectedPort.getPosition().y, 2)
                );
                
                isValidConnection = wireLength <= game.getRemainingWireLength();
            }
            
            
            g2d.setColor(isValidConnection ? 
                         Constants.WIRE_ALLOWABLE_COLOR : 
                         Constants.WIRE_UNALLOWABLE_COLOR);
            
            g2d.setStroke(new BasicStroke(Constants.WIRE_THICKNESS));
            Point sourcePos = selectedPort.getPosition();
            g2d.drawLine(sourcePos.x, sourcePos.y, wireEndPoint.x, wireEndPoint.y);
        }
        
        
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