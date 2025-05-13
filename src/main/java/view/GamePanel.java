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
                if (!isGameRunning) {
                    game.rewindTime();
                    updateHUD();
                    repaint();
                }
            }
        });
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
        
        add(wireLabel);
        add(temporalLabel);
        add(packetLossLabel);
        add(coinsLabel);
        add(instructionLabel);
        
        // Add component listener to reposition instruction label when panel resizes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                instructionLabel.setBounds(20, getHeight() - 50, 600, 30);
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
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isGameRunning || !isWiring) return;
                
                isWiring = false;
                Port targetPort = findPortAt(e.getPoint());
                if (targetPort != null && targetPort.isInput() && 
                    selectedPort.getParentSystem() != targetPort.getParentSystem()) {
                    
                    Wire newWire = new Wire(selectedPort, targetPort);
                    int wireLength = newWire.getLength();
                    
                    if (wireLength <= game.getRemainingWireLength()) {
                        game.addWire(newWire);
                        wires.add(newWire);
                        updateHUD();
                        SoundManager.getInstance().playSound("connection");
                    }
                }
                
                selectedPort = null;
            }
        });
        
        // Mouse motion listener for updating wire end point
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isWiring) {
                    wireEndPoint = e.getPoint();
                    repaint();
                }
            }
        });
        
        // Key listener for game controls
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        if (!isGameRunning) game.advanceTime();
                        break;
                    case KeyEvent.VK_LEFT:
                        if (!isGameRunning) game.rewindTime();
                        break;
                    case KeyEvent.VK_SPACE:
                        toggleGameRunning();
                        break;
                    case KeyEvent.VK_H:
                        toggleHUD();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        if (isGameRunning) {
                            isGameRunning = false;
                        }
                        break;
                    case KeyEvent.VK_S:
                        showShop();
                        break;
                }
                updateHUD();
                repaint();
            }
        });
    }
    
    private void showShop() {
        if (!isGameRunning) return; // Shop only available during gameplay
        
        game.setPaused(true);
        ShopView shopView = new ShopView(GameFrame.getINSTANCE());
        shopView.setVisible(true);
        game.setPaused(false);
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
            60, // Generate a packet every 60 frames
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
            60, 
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
            80, 
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
            SoundManager.getInstance().playSound("game_start");
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
            game.update();
            
            if (game.isGameOver()) {
                isGameRunning = false;
                showGameOverDialog();
            }
            
            updateHUD();
        }
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
            g2d.setColor(game.getRemainingWireLength() > 0 ? 
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