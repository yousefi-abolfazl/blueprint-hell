package view;

import controller.Constants;
import controller.SceneController;
import controller.SoundManager;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class GamePanel extends JPanel {
    private static GamePanel INSTANCE;
    private Game game;
    private Port selectedPort;
    private Point wireEndPoint;
    private boolean isWiring = false;
    private boolean isGameRunning = false;

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
        
        initializeHUD();
        initializeControls();
        
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

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "returnToMenu");
        actionMap.put("returnToMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Escape key pressed - returning to main menu.");
                SceneController.getInstance().returnToMainMenu();
            }
        });
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
                        // game.addWire(newWire);
                        Game.getInstance().addWire(newWire); 
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
                repaint();
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
        for (NetworkSystem system : game.getSystems()) {
            
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
        int width = getWidth();
        int height = getHeight();
        
        int padding = 200;
        int systemWidth = 100;
        int systemHeight = 80;


        SourceSystem sourceTL = new SourceSystem(
            new Point(padding, padding), systemWidth, systemHeight, 80, true);
        sourceTL.addOutputPort(new SquarePort(
            new Point(sourceTL.getPosition().x + systemWidth, sourceTL.getPosition().y + 40), false, sourceTL));
        game.addSystem(sourceTL);

        SourceSystem sourceTR = new SourceSystem(
            new Point(width - padding - systemWidth, padding), systemWidth, systemHeight, 90, false);
        sourceTR.addOutputPort(new TrianglePort(
            new Point(sourceTR.getPosition().x, sourceTR.getPosition().y + 40), false, sourceTR));
        game.addSystem(sourceTR);

        SourceSystem sourceBL = new SourceSystem(
            new Point(padding, height - padding - systemHeight), systemWidth, systemHeight, 90, false);
        sourceBL.addOutputPort(new TrianglePort(
            new Point(sourceBL.getPosition().x + systemWidth, sourceBL.getPosition().y + 40), false, sourceBL));
        game.addSystem(sourceBL);

        SourceSystem sourceBR = new SourceSystem(
            new Point(width - padding - systemWidth, height - padding - systemHeight), systemWidth, systemHeight, 80, true);
        sourceBR.addOutputPort(new SquarePort(
            new Point(sourceBR.getPosition().x, sourceBR.getPosition().y + 40), false, sourceBR));
        game.addSystem(sourceBR);



        StandardSystem transitTop = new StandardSystem(
            new Point(width / 2 - (systemWidth / 2), height / 2 - padding), systemWidth, systemHeight);
        transitTop.addInputPort(new SquarePort(new Point(transitTop.getPosition().x, transitTop.getPosition().y + 20), true, transitTop));
        transitTop.addInputPort(new TrianglePort(new Point(transitTop.getPosition().x + systemWidth, transitTop.getPosition().y + 20), true, transitTop));
        transitTop.addOutputPort(new SquarePort(new Point(transitTop.getPosition().x + 50, transitTop.getPosition().y + systemHeight), false, transitTop));
        game.addSystem(transitTop);
        
        StandardSystem transitBottom = new StandardSystem(
            new Point(width / 2 - (systemWidth / 2), height / 2 + padding - systemHeight), systemWidth, systemHeight);
        transitBottom.addInputPort(new TrianglePort(new Point(transitBottom.getPosition().x, transitBottom.getPosition().y + 20), true, transitBottom));
        transitBottom.addInputPort(new SquarePort(new Point(transitBottom.getPosition().x + systemWidth, transitBottom.getPosition().y + 20), true, transitBottom));
        transitBottom.addOutputPort(new TrianglePort(new Point(transitBottom.getPosition().x + 50, transitBottom.getPosition().y), false, transitBottom));
        game.addSystem(transitBottom);


        DestinationSystem destination = new DestinationSystem(
            new Point(width / 2 - (systemWidth / 2), height / 2 - (systemHeight / 2)), systemWidth, systemHeight);
        destination.addInputPort(new SquarePort(new Point(destination.getPosition().x + 20, destination.getPosition().y), true, destination));
        destination.addInputPort(new TrianglePort(new Point(destination.getPosition().x + 80, destination.getPosition().y + systemHeight), true, destination));
        game.addSystem(destination);
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
        
        
        game.addSystem(squareDest);
        game.addSystem(triangleDest);
    }
    
    private void toggleGameRunning() {
        System.out.println("--- toggleGameRunning called! Changing isGameRunning from " +
                isGameRunning + " to " + 
                !isGameRunning + " ---");
        isGameRunning = !isGameRunning;

        if (isGameRunning) {
            System.out.println("Game started running");

            // از game.getSystems() برای دسترسی به مدل استفاده کنید
            for (NetworkSystem system : game.getSystems()) {
                if (system instanceof SourceSystem) {
                    System.out.println("Activating source system");
                    system.setActive(true);
                    
                    SourceSystem sourceSystem = (SourceSystem) system;
                    sourceSystem.randomizePacketCounter();
                }
            }

            SoundManager.getInstance().playSound("game_start");

        } else {
            System.out.println("Game paused");
            SoundManager.getInstance().playSound("game_pause");
        }

        // این دو خط را از اینجا حذف نکنید، چون مستقیماً بر روی نما تأثیر دارند
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
        
        
        for (Wire wire : game.getWires()) {
            wire.render(g2d);
        }
        
        
        for (NetworkSystem system : game.getSystems()) {
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
        
        
        if (!isWiring && !isGameRunning && game.getSystems().size() > 0) {
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String instructions = "Connect output ports (RED) to input ports (GREEN) by clicking and dragging";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(instructions);
            g2d.drawString(instructions, (width - textWidth) / 2, height - 100);
        }
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public void setGameRunning(boolean isRunning) {
        System.out.println("!!! setGameRunning called! New value: " + isRunning);
        this.isGameRunning = isRunning;
    }
    
    public static GamePanel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GamePanel();
        }
        return INSTANCE;
    }
}